import sbt.Keys._
import sbt._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._


inThisBuild(List(
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.12.12", "2.13.1"),
  organization := "com.av8data",
  homepage := Some(url("https://github.com/av8data/finatra-swagger")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "mattav8data",
      "Matthew Dickinson ",
      "matt@av8data.com",
      url("https://av8data.com")
    ),
    Developer(id="jakehschwartz", name="Jake Schwartz", email="jakehschwartz@gmail.com", url=url("https://www.jakehschwartz.com")),
    Developer(id="xiaodongw", name="Xiaodong Wang", email="xiaodongw79@gmail.com", url=url("https://github.com/xiaodongw"))
  ),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/av8data/finatra-swagger"),
      connection = "https://github.com/av8data/finatra-swagger"
    )
  ),
  publishTo := Some(
    "releases" at "https://oss.sonatype.org/" + "service/local/staging/deploy/maven2"),
))

showCurrentGitBranch
git.useGitDescribe := true
git.baseVersion := "0.0.0"
val VersionRegex = "v([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r
git.gitTagToVersionNumber := {
  case VersionRegex(v, "SNAPSHOT") => Some(s"$v-SNAPSHOT")
  case VersionRegex(v, "") => Some(v)
  case VersionRegex(v, s) => Some(v)
  case v => None
}

(sys.env.get("SONATYPE_USERNAME"), sys.env.get("SONATYPE_PASSWORD")) match {
  case (Some(username), Some(password)) =>
    println(s"Using credentials: $username/$password")
    credentials += Credentials(
      realm = "Sonatype Nexus Repository Manager",
      host = "oss.sonatype.org",
      userName = username,
      passwd = password)
  case _ =>
    println("USERNAME and/or PASSWORD is missing, using local credentials")
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
}

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Compile := true,
  publishArtifact in Test := false,
  autoAPIMappings := true
)

credentials += Credentials(
  realm = "GnuPG Key ID",
  host = "gpg",
  userName = "B98040CCE81E1A52F3358BED4391E8775B145A81", // key identifier
  passwd = "ignored"
)

pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray())

releaseVersionBump := sbtrelease.Version.Bump.Next
releaseVersion := { ver =>
  Version(ver)
    .map(_.bump(releaseVersionBump.value).string)
    .getOrElse(versionFormatError(ver))
}

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  runTest,
  tagRelease,
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  pushChanges
)


lazy val sharedSettings = Seq(
  organization := "com.av8data",
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
)

lazy val swaggerUIVersion = SettingKey[String]("swaggerUIVersion")
lazy val finatraSwagger = project
  .in(file("."))
  .settings(settings: _*)
  .settings(sharedSettings)
  .settings(publishSettings)
  .settings(Seq(
    name := "finatra-swagger",
    swaggerUIVersion := "3.50.0",
    buildInfoPackage := "com.av8data.finatra.swagger",
    buildInfoKeys := Seq[BuildInfoKey](name, version, swaggerUIVersion),
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http-server" % twitterReleaseVersion,
      "io.swagger.core.v3" % "swagger-project" % "2.1.9",
      "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.3.1",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
      "org.webjars" % "swagger-ui" % swaggerUIVersion.value,
      "net.bytebuddy" % "byte-buddy" % "1.11.5"
    ) ++ testLibs
  ))
  .settings(settings: _*)
  .enablePlugins(BuildInfoPlugin)

lazy val examples = Project("hello-world-example", file("examples/hello-world"))
  .settings(Seq(
    name := "examples",
    libraryDependencies ++= testLibs,
  ))
  .settings(settings: _*)
  .settings(skip in publish := true)
  .dependsOn(finatraSwagger)

lazy val settings: Seq[sbt.Def.SettingsDefinition] = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-language:existentials",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    "Local Ivy" at "file:///"+Path.userHome+"/.ivy2/local",
    "Local Ivy Cache" at "file:///"+Path.userHome+"/.ivy2/cache",
    "Local Maven Repository" at "file:///"+Path.userHome+"/.m2/repository"
  ),
  parallelExecution in Test := true,
  testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  javaOptions ++= Seq(
    "-Xss8M",
    "-Xms512M",
    "-Xmx2G"
  ),
  javaOptions in Test ++= Seq(
    "-Dlog.service.output=/dev/stdout",
    "-Dlog.access.output=/dev/stdout",
    "-Dlog_level=DEBUG"
  )
)


lazy val twitterReleaseVersion = "21.4.0"
lazy val jacksonVersion = "2.11.2"
val testLibs = Seq(
  "com.twitter" %% "finatra-http-server" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-app" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-core" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-server" % twitterReleaseVersion % "test" classifier "tests",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "org.mockito" %% "mockito-scala" % "1.16.37" % "test"
)

val exampleLibs = Seq(
  "com.av8data" %% "finatra-swagger" % twitterReleaseVersion,
)
