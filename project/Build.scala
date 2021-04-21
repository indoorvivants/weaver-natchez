import sbt._
import sbt.Keys._
object Build {

  case class CatsEffectAxis(idSuffix: String, directorySuffix: String)
      extends VirtualAxis.WeakAxis

  val CatsEffect2Axis = CatsEffectAxis("_CE2", "-ce2")
  val CatsEffect3Axis = CatsEffectAxis("_CE3", "-ce3")

  val scala213 = "2.13.5"

  val scalas = Seq("2.12.13", scala213, "3.0.0-RC2")

  trait Versions {
    val weaver: String
    val catsEffect: String
    val natchez: String
  }

  val CE2_Versions = new Versions {
    val weaver     = "0.6.1"
    val catsEffect = "2.5.0"
    val natchez    = "0.0.22"
  }

  val CE3_Versions = new Versions {
    val weaver     = "0.7.1"
    val catsEffect = "3.1.0"
    val natchez    = "0.1.0"
  }

  val cats = "2.6.0"

  def dependencies(V: Versions) = Seq(
    libraryDependencies += "org.typelevel" %% "cats-core"   % cats,
    libraryDependencies += "org.typelevel" %% "cats-effect" % V.catsEffect,
    libraryDependencies ++= {
      if (V.catsEffect.startsWith("3"))
        Seq("org.typelevel" %% "cats-effect-kernel" % V.catsEffect)
      else Seq()
    },
    libraryDependencies += "org.tpolecat"        %% "natchez-core"     % V.natchez,
    libraryDependencies += "com.disneystreaming" %% "weaver-cats"      % V.weaver % Test,
    libraryDependencies += "com.disneystreaming" %% "weaver-cats-core" % V.weaver,
    libraryDependencies += "com.disneystreaming" %% "weaver-core"      % V.weaver
  )

  def increaseVersion(original: String): String = {

    val regex = "^(\\d+).(\\d+).(\\d+).*$".r

    original match {
      case regex(major, minor, patch) =>
        original.replaceFirst(
          s"$major.$minor.$patch",
          s"$major.${minor.toInt + 1}.$patch"
        )
      case _ =>
        throw new RuntimeException(
          s"Version $original doesn't match SemVer format"
        )
    }
  }

  lazy val versionOverrideForCE3: Seq[Def.Setting[_]] = Seq(
    version := {
      increaseVersion(version.value)
    }
  )

}
