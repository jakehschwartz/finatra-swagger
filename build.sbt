
inThisBuild(List(
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.12.12", "2.13.1"),
  organization := "com.jakehschwartz",
  homepage := Some(url("https://github.com/jakehschwartz/finatra-swagger")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(id="jakehschwartz", name="Jake Schwartz", email="jakehschwartz@gmail.com", url=url("https://www.jakehschwartz.com")),
    Developer(id="xiaodongw", name="Xiaodong Wang", email="xiaodongw79@gmail.com", url=url("https://github.com/xiaodongw"))
  )
))

lazy val swaggerUIVersion = SettingKey[String]("swaggerUIVersion")
lazy val finatraSwagger = project
  .in(file("."))
  .settings(settings: _*)
  .settings(Seq(
    name := "finatra-swagger",
    swaggerUIVersion := "3.47.1",
    buildInfoPackage := "com.jakehschwartz.finatra.swagger",
    buildInfoKeys := Seq[BuildInfoKey](name, version, swaggerUIVersion),
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http-server" % twitterReleaseVersion,
      "io.swagger.core.v3" % "swagger-project" % "2.1.9",
      "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.3.0",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
      "org.webjars" % "swagger-ui" % swaggerUIVersion.value,
      "net.bytebuddy" % "byte-buddy" % "1.10.22"
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


lazy val twitterReleaseVersion = "21.6.0"
lazy val jacksonVersion = "2.11.2"
val testLibs = Seq(
  "com.twitter" %% "finatra-http-server" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-app" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-core" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % twitterReleaseVersion % "test" classifier "tests",
  "com.twitter" %% "inject-server" % twitterReleaseVersion % "test" classifier "tests",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.2.8" % "test",
  "org.mockito" %% "mockito-scala" % "1.16.37" % "test"
)

val exampleLibs = Seq(
  "com.jakehschwartz" %% "finatra-swagger" % twitterReleaseVersion,
)
