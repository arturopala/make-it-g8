// This build is for this Giter8 template.
// To test the template run the script `./test.sh`
// See http://www.foundweekends.org/giter8/testing.html#Using+the+Giter8Plugin for more details.
lazy val root = (project in file(".")).settings(
  name := "$templateName$.g8",
  test in Test := {
    val _ = (g8Test in Test).toTask("").value
  },
  scriptedLaunchOpts ++= List(
    "-Xms1024m",
    "-Xmx1024m",
    "-XX:ReservedCodeCacheSize=128m",
    "-XX:MaxPermSize=256m",
    "-Xss2m",
    "-Dfile.encoding=UTF-8"),
  resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(
    Resolver.ivyStylePatterns)
)
