// To sync with Maven central, you need to supply the following information:
sonatypeProfileName := "com.github.arturopala"

pomExtra in Global := {
  <url>github.com/arturopala/make-it-g8</url>
  <scm>
    <connection>https://github.com/arturopala/make-it-g8.git</connection>
    <developerConnection>git@github.com:arturopala/make-it-g8.git</developerConnection>
    <url>github.com/arturopala/make-it-g8</url>
  </scm>
  <developers>
    <developer>
      <id>arturopala</id>
      <name>Artur Opala</name>
      <url>https://pl.linkedin.com/in/arturopala</url>
    </developer>
  </developers>
}

publishMavenStyle := true

import ReleaseTransformations._

releaseCrossBuild := false
releaseUseGlobalVersion := true
releaseVersionBump := sbtrelease.Version.Bump.Minor

usePgpKeyHex("D9267F3ECB3CF847330BA02AAAC19B29BEF3DCBF")

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
