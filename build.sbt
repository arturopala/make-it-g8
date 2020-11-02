ThisBuild / scalaVersion := "2.12.12"
ThisBuild / organization := "com.github.arturopala"
ThisBuild / organizationName := "Artur Opala"
ThisBuild / startYear := Some(2020)

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(ScriptedPlugin.globalSettings)
  .settings(ScriptedPlugin.projectSettings)
  .settings(
    sbtPlugin := true,
    name := "make-it-g8",
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    libraryDependencies ++= Seq(
      "com.typesafe"         % "config"        % "1.4.0",
      "com.github.pathikrit" %% "better-files" % "3.8.0",
      "org.rogach"           %% "scallop"      % "3.5.1",
      "org.scalatest"        %% "scalatest"    % "3.1.1" % Test
    ),
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    excludeFilter in (Compile, unmanagedResources) := NothingFilter,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    publishTo := sonatypePublishTo.value,
    mainClass in (Compile, run) := Some("com.github.arturopala.makeitg8.MakeItG8")
  )
