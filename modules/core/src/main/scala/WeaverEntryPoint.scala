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
import cats.effect.concurrent.Ref
import cats.syntax.all._
import natchez.EntryPoint
import natchez.Kernel
import natchez.Span
import natchez.TraceValue

class WeaverEntryPoint[F[_]: Sync](base: RefTree[F, Long, String])
    extends EntryPoint[F] {
  override def root(name: String): Resource[F, Span[F]] = Resource.liftF {
    for {
      mt     <- Monotonic.refBased
      rootId <- mt.id
      params <- Ref.of[F, Map[String, TraceValue]](Map.empty)
    } yield new WeaverSpan[F](rootId, mt, params, base)
  }

  override def continue(name: String, kernel: Kernel): Resource[F, Span[F]] =
    ???

  override def continueOrElseRoot(
      name: String,
      kernel: Kernel
  ): Resource[F, Span[F]] = ???

}

object WeaverEntryPoint {
  def create[F[_]: Sync] =
    RefTree.empty[F, Long, String].map { ref =>
      new WeaverEntryPoint[F](ref) -> ref
    }
}
