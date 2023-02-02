inThisBuild(
  List(
    scalaVersion := "2.12.17",
    organization := "com.github.arturopala",
    organizationName := "Artur Opala",
    homepage := Some(url("https://github.com/arturopala/make-it-g8")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "arturopala",
        "Artur Opala",
        "opala.artur@gmail.com",
        url("https://www.linkedin.com/in/arturopala")
      )
    ),
    startYear := Some(2020)
  )
)

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "make-it-g8",
    libraryDependencies ++= Seq(
      "com.typesafe"          % "config"       % "1.4.1",
      "com.github.pathikrit" %% "better-files" % "3.9.1",
      "org.rogach"           %% "scallop"      % "4.0.2",
      "org.scalatest"        %% "scalatest"    % "3.2.3" % Test
    ),
    excludeFilter in (Compile, unmanagedResources) := NothingFilter,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    mainClass in (Compile, run) := Some("com.github.arturopala.makeitg8.MakeItG8"),
    versionScheme := Some("early-semver")
  )
