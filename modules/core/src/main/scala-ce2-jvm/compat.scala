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

package cats.effect.compat

object all {
  type Ref[F[_], A] = cats.effect.concurrent.Ref[F, A]

  def createRef[F[_]: cats.effect.Sync, A](init: A): F[Ref[F, A]] =
    cats.effect.concurrent.Ref.of[F, A](init)

  def liftResource[F[_]: cats.Applicative, A](
      fa: F[A]
  ): cats.effect.Resource[F, A] =
    cats.effect.Resource.liftF(fa)
}
