package wrenches
package examples.skunk

import skunk.Session
import cats.effect.{IO, Resource}
import wrenchez.WeaverEntryPoint
import cats.syntax.all._
import cats.arrow.FunctionK

object SkunkTests extends weaver.IOSuite {
  override type Res = (Session[IO], wrenchez.Tracer)
  override def sharedResource: Resource[IO, Res] =
    Resource
      .liftF(
        wrapSkunk(
          Session.single[traced](
            host = "localhost",
            port = 5432,
            user = "jimmy",
            database = "world",
            password = Some("banana")
          )
        )
      )
      .flatten

  // This is my first approach which *sort* of works.
  // The problem is - the structure of the spans is set at this point, so you can only really
  // run Skunk's tracing - your own traces will not be respected
  type traced[A] = cats.data.Kleisli[IO, natchez.Span[IO], A]
  def wrapSkunk(sess: Resource[traced, Session[traced]]) = {
    WeaverEntryPoint.create[IO].map { case (ep, _) =>
      ep.newRoot.flatMap { span =>
        val ff: FunctionK[traced, IO] = new FunctionK[traced, IO] {
          def apply[A](fa: traced[A]): IO[A] = fa.run(span)
        }

        val step1 = sess.mapK[traced, IO](ff)

        val step2 = step1.map[IO, Session[IO]](_.mapK(ff))

        step2.fproduct(_ => new wrenchez.Tracer(span))
      }
    }
  }

  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  // a data model
  case class Country(name: String, code: String, population: Int)

  val extended: Query[String, Country] =
    sql"""
      SELECT name, code, population
      FROM   country
      WHERE  name like $text
    """
      .query(varchar ~ bpchar(3) ~ int4)
      .gmap[Country]

  def doExtended(s: Session[IO]): IO[Unit] =
    s.prepare(extended).use { ps =>
      ps.stream("U%", 32).compile.drain
    }

  test("hello") { (resources, log) =>
    val (skunk, tracer) = resources

    tracer(log) {
      for {
        _ <- skunk.unique(sql"select current_date".query(date)) // (4)
      } yield failure("testing")
    }

  }

  test("extended hello") { (resources, log) =>
    val (skunk, tracer) = resources

    tracer(log) {
      for {
        _ <- log.info("Howdy")
        _ <- doExtended(skunk)
      } yield failure("yup")
    }
  }

}
