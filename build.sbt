import Build._

lazy val root = project
  .in(file("."))
  .aggregate(core.projectRefs: _*)
  .aggregate(docs)
  .settings(
    skip in publish := true
  )

lazy val core = projectMatrix
  .in(file("modules/core"))
  .defaultAxes(VirtualAxis.jvm, CatsEffect2Axis, VirtualAxis.scalaABIVersion("2.13"))
  .settings(
    name := "wrenchez-core",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .customRow(
    scalaVersions = scalas,
    axisValues = Seq(VirtualAxis.jvm, CatsEffect2Axis),
    process = _.settings(dependencies(CE2_Versions))
  )
  .customRow(
    scalaVersions = scalas,
    axisValues = Seq(VirtualAxis.jvm, CatsEffect3Axis),
    process =
      _.settings(dependencies(CE3_Versions)).settings(versionOverrideForCE3)
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
      (baseDirectory in ThisBuild).value / "docs",
    subatomicMdocVariables := Map(
      "CE2_VERSION" -> (core.finder(CatsEffect2Axis)(true) / version).value,
      "CE3_VERSION" -> (core.finder(CatsEffect3Axis)(true) / version).value
    )
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
