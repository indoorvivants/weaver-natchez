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

import cats.effect._
import cats.effect.compat.all._
import cats.syntax.all._
import natchez.Kernel
import natchez.Span
import natchez.TraceValue

import java.net.URI

object WeaverSpan {
  case class SpanState[F[_]](
      name: String,
      params: Ref[F, Map[String, natchez.TraceValue]]
  )
}

import WeaverSpan._

class WeaverSpan[F[_]: Sync](
    private val id: Long,
    mt: Monotonic[F],
    rf: Ref[F, Map[String, TraceValue]],
    private[wrenchez] val parent: RefTree[F, Long, SpanState[F]]
) extends Span[F] {
  override def put(fields: (String, TraceValue)*): F[Unit] = {
    rf.update(_ ++ fields)
  }

  override def kernel: F[Kernel] = Kernel(Map.empty).pure[F]

  override def span(name: String): Resource[F, Span[F]] =
    liftResource {
      for {
        newId     <- mt.id
        paramsRef <- createRef[F, Map[String, TraceValue]](Map.empty)
        newparent <- parent.nest(newId, SpanState(name, paramsRef))
        span = new WeaverSpan[F](
          newId,
          mt,
          paramsRef,
          newparent
        )
      } yield span
    }

  def span_(name: String): Resource[F, WeaverSpan[F]] =
    liftResource {
      for {
        newId     <- mt.id
        paramsRef <- createRef[F, Map[String, TraceValue]](Map.empty)
        newparent <- parent.nest(newId, SpanState(name, paramsRef))
        span = new WeaverSpan[F](
          newId,
          mt,
          paramsRef,
          newparent
        )
      } yield span
    }

  override def spanId: F[Option[String]] = Option.empty.pure[F]

  override def traceId: F[Option[String]] = Option.empty.pure[F]

  override def traceUri: F[Option[URI]] = Option.empty.pure[F]
}
