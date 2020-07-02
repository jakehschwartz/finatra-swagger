name := "finatra-swagger"

organization := "com.jakehschwartz"

scalaVersion := "2.12.11"

crossScalaVersions := Seq("2.11.12", "2.12.11")

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val twitterReleaseVersion = "20.6.0"
lazy val jacksonVersion = "2.10.4"

lazy val swaggerUIVersion = SettingKey[String]("swaggerUIVersion")

swaggerUIVersion := "3.24.3"

enablePlugins(BuildInfoPlugin)
buildInfoPackage := "com.jakehschwartz.finatra.swagger"
buildInfoKeys := Seq[BuildInfoKey](name, version, swaggerUIVersion)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % twitterReleaseVersion excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "io.swagger" %% "swagger-scala-module" % "1.0.6",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "org.webjars" % "swagger-ui" % swaggerUIVersion.value,
  "net.bytebuddy" % "byte-buddy" % "1.10.5",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

val examplesTestLibs = Seq(
  "com.twitter" %% "finatra-http" % twitterReleaseVersion % "test" classifier "tests" excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "com.twitter" %% "inject-app" % twitterReleaseVersion % "test" classifier "tests" excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "com.twitter" %% "inject-core" % twitterReleaseVersion % "test" classifier "tests" excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "com.twitter" %% "inject-modules" % twitterReleaseVersion % "test" classifier "tests" excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "com.twitter" %% "inject-server" % twitterReleaseVersion % "test" classifier "tests" excludeAll(
    "com.fasterxml.jackson.core",
    "com.fasterxml.jackson.dataformat",
    "com.fasterxml.jackson.datatype",
    "com.fasterxml.jackson.module"
  ),
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.8"  % Test,
  "org.mockito" % "mockito-all" % "1.10.19"  % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions"
)

pomIncludeRepository := { _ => false }

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", ""),
  sys.env.getOrElse("SONATYPE_PASSWORD", "")
)

pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray())

// License of your choice
licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://(your project url)"))
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/jakehschwartz/finatra-swagger"),
    connection = "https://github.com/jakehschwartz/finatra-swagger.git"
  )
)
developers := List(
  Developer(id="jakehschwartz", name="Jake Schwartz", email="jakehschwartz@gmail.com", url=url("https://www.jakehschwartz.com")),
  Developer(id="xiaodongw", name="Xiaodong Wang", email="xiaodongw79@gmail.com", url=url("https://github.com/xiaodongw"))
)

lazy val root = Project("finatra-swagger", file("."))

lazy val example = Project("hello-world-example", file("examples/hello-world"))
  .dependsOn(root)
  .settings(libraryDependencies ++= examplesTestLibs)
  .settings(
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
      "-Dlog_level=DEBUG")
  )
