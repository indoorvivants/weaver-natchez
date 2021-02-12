lazy val root = project.in(file("."))
  .aggregate(core.projectRefs:_*)
  .aggregate(docs)
  .settings(
    skip in publish := true
  )

lazy val V = new {
  val weaver  = "0.6.0-M6"
  val cats    = "2.3.1"
  val natchez = "0.0.19"
}

lazy val core = projectMatrix
  .in(file("modules/core"))
  .jvmPlatform(scalaVersions = Seq("2.12.13", "2.13.4", "3.0.0-M3"))
  .settings(
    name := "wrenchez-core",
    libraryDependencies += "org.typelevel"       %% "cats-core"        % V.cats,
    libraryDependencies += "org.typelevel"       %% "cats-effect"      % V.cats,
    libraryDependencies += "org.tpolecat"        %% "natchez-core"     % V.natchez,
    libraryDependencies += "com.disneystreaming" %% "weaver-cats"      % V.weaver % Test,
    libraryDependencies += "com.disneystreaming" %% "weaver-cats-core" % V.weaver,
    libraryDependencies += "com.disneystreaming" %% "weaver-core"      % V.weaver,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .settings(buildSettings)

lazy val docs = project
  .in(file("docs"))
  .settings(scalaVersion := "2.13.4")
  .dependsOn(core.jvm("2.13.4"))
  .enablePlugins(SubatomicPlugin)
  .settings(
    unusedCompileDependenciesTest := {},
    undeclaredCompileDependenciesTest := {},
    skip in publish := true,
    watchSources += WatchSource(
      (baseDirectory in ThisBuild).value / "docs" / "pages"
    ),
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in ThisBuild).value / "docs"
  )

// ---------- CONFIG

lazy val buildSettings = Seq(
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("3.")) Seq.empty
    else
      Seq(
        compilerPlugin(
          "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full
        ),
        compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
      )
  }
)

inThisBuild(
  List(
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := scalaBinaryVersion.value,
    organization := "com.indoorvivants",
    organizationName := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/weaver-natchez")
    ),
    startYear := Some(2021),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "keynmol@gmail.com",
        url("https://blog.indoorvivants.com")
      )
    )
  )
)

val scalafixRules = Seq(
  "OrganizeImports",
  "DisableSyntax",
  "LeakingImplicitClassVal",
  "ProcedureSyntax",
  "NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
  "clean",
  "compile",
  "test",
  "core/scalafmtCheckAll",
  s"core/scalafix --check $scalafixRules",
  "core/headerCheck",
  "undeclaredCompileDependenciesTest",
  "unusedCompileDependenciesTest",
  "core/missinglinkCheck"
).mkString(";")

val PrepareCICommands = Seq(
  s"core/compile:scalafix --rules $scalafixRules",
  s"core/test:scalafix --rules $scalafixRules",
  "core/test:scalafmtAll",
  "core/compile:scalafmtAll",
  "core/scalafmtSbt",
  "core/headerCreate",
  "undeclaredCompileDependenciesTest"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)
