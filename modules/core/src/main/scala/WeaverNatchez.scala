/*
 * Copyright 2021 Anton Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wrenchez

import cats.effect.IO
import natchez.TraceValue._
import natchez._
import weaver._

class Tracer(rootSpan: WeaverSpan[IO]) {
  def apply(log: weaver.Log[IO])(exp: IO[Expectations]): IO[Expectations] = {
    // rootSpan.span_(java.util.UUID.randomUUID().toString).use { _ =>
    exp.guarantee(
      rootSpan.parent.traverse { (level, _, v) =>
        v match {
          case None => IO.unit
          case Some(WeaverSpan.SpanState(name, paramsRef)) =>
            paramsRef.get.flatMap { params =>
              log.debug(
                Console.GREEN + ("." * level) + Console.RESET + Console.YELLOW + s" [$name]" + Console.RESET,
                params.map {
                  case (k, StringValue(str)) =>
                    k -> str
                  //.replace("\n", Console.BLUE + "\\n" + Console.RESET)
                  case (k, BooleanValue(b)) => k -> b.toString
                  case (k, NumberValue(n))  => k -> n.toString
                }
              )

            }
        }
      }
    )
    // }
  }
}

trait WeaverNatchez { self: weaver.IOSuite =>

  def tracedTest(name: String)(f: Span[IO] => IO[weaver.Expectations]) = {
    loggedTest(name) { log =>
      WeaverEntryPoint.create[IO].flatMap { case (ep, ref) =>
        ep.root(name).use { span =>
          f(span).guarantee {
            ref
              .traverse { (level, _, v) =>
                log.debug(
                  Console.GREEN + ("." * level) + Console.RESET + s" [${v.getOrElse("<unnamed>")}]"
                )
              }
          }
        }
      }
    }
  }

}

object WeaverNatches {
  def dump[F[_], K, V](log: Log[F], rt: RefTree[F, K, V]) = {
    rt
      .traverse { (level, _, v) =>
        log.debug(
          Console.GREEN + ("." * level) + Console.RESET + s" [${v.getOrElse("<unnamed>")}]"
        )
      }
  }
}
