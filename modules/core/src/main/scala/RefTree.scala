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
import cats.effect.concurrent._
import cats.syntax.all._

case class RefTree[F[_]: Sync, K, V](
    label: Option[V],
    children: Ref[F, Map[K, Ref[F, RefTree[F, K, V]]]]
) {
  def nest(k: K, lab: Option[V]): F[RefTree[F, K, V]] = for {

    childrenRef <- RefTree.childrenRef[F, K, V]
    newTree     <- Ref.of[F, RefTree[F, K, V]](RefTree(lab, childrenRef))
    _           <- children.updateAndGet(_.updated(k, newTree))
    res         <- newTree.get
  } yield res

  def traverse(f: (Int, K, Option[V]) => F[Unit]): F[Unit] = {
    def go(level: Int, cur: RefTree[F, K, V]): F[Unit] =
      cur.children.get.flatMap { mt =>
        val xr = mt.toList
        xr.traverse_ { case (newK, newRef) =>
          newRef.get.flatMap(rf =>
            f(level + 1, newK, rf.label) *> go(level + 1, rf)
          )
        }
      }

    go(0, this)
  }

  def nest(k: K): F[RefTree[F, K, V]]       = nest(k, Option.empty)
  def nest(k: K, v: V): F[RefTree[F, K, V]] = nest(k, Option(v))
}

object RefTree {
  private type Children[F[_], K, V] = Map[K, Ref[F, RefTree[F, K, V]]]
  def childrenRef[F[_]: Sync, K, V]: F[Ref[F, Children[F, K, V]]] = {
    Ref.of[F, Children[F, K, V]](Map.empty)
  }

  def empty[F[_]: Sync, K, V]: F[RefTree[F, K, V]] = childrenRef.map {
    (rf: Ref[F, Children[F, K, V]]) =>
      RefTree(None, rf)
  }
}
