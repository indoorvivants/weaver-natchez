package wrenchez

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.Sync
import cats.syntax.all._
import natchez.Span
import natchez.Trace

object Tests extends weaver.SimpleIOSuite with WeaverNatchez {

  type Traced[A] = Kleisli[IO, Span[IO], A]

  tracedTest("demonstration") { trace =>
    largeProgF[Traced].run(trace).map { results =>
      expect(results.contains(5))
    }
  }

  def randomInt[F[_]: Sync] = Sync[F].delay(scala.util.Random.nextInt())

  def dumpNumber[F[_]: Sync: Trace]: F[Int] =
    randomInt[F].flatMap { n =>
      if (n % 3 == 0) {
        Trace[F].span(s"$n was generated (and it's divisble by 3!)") {
          n.pure[F]
        }
      } else
        Trace[F].span(s"$n is not even divisible by 3, trying again") {
          dumpNumber[F]
        }
    }

  def progF[F[_]: Sync: Trace]: F[Int] = Trace[F].span("program") {
    Trace[F].span("starting") {
      dumpNumber[F]
    }
  }

  def largeProgF[F[_]: Sync: Trace]: F[List[Int]] = {
    (1 to 5).toList.traverse(_ => progF[F])
  }
}
