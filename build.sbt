ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.github.arturopala"
ThisBuild / organizationName := "Artur Opala"
ThisBuild / startYear := Some(2020)

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "make-it-g8",
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    libraryDependencies ++= Seq(
      "com.typesafe"          % "config"       % "1.4.1",
      "com.github.pathikrit" %% "better-files" % "3.9.1",
      "org.rogach"           %% "scallop"      % "4.0.2",
      "org.scalatest"        %% "scalatest"    % "3.2.3" % Test
    ),
    excludeFilter in (Compile, unmanagedResources) := NothingFilter,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    publishTo := sonatypePublishToBundle.value,
    mainClass in (Compile, run) := Some("com.github.arturopala.makeitg8.MakeItG8")
  )
