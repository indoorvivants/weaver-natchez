package cats.effect.compat

object all {
  def createRef[F[_]: cats.effect.Sync, A](init: A): F[cats.effect.Ref[F, A]] =
    cats.effect.Ref.of[F, A](init)

  def liftResource[F[_], A](fa: F[A]): cats.effect.Resource[F, A] = cats.effect.Resource.eval(fa)
}
