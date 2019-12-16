name := "finatra-swagger"

organization := "com.jakehschwartz"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.11.12", "2.12.10")

lazy val swaggerUIVersion = SettingKey[String]("swaggerUIVersion")

swaggerUIVersion := "3.24.3"

enablePlugins(BuildInfoPlugin)
buildInfoPackage := "com.jakehschwartz.finatra.swagger"
buildInfoKeys := Seq[BuildInfoKey](name, version, swaggerUIVersion)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "19.12.0",
  "io.swagger" % "swagger-core" % "1.5.24",
  "io.swagger" %% "swagger-scala-module" % "1.0.6",
  "org.webjars" % "swagger-ui" % swaggerUIVersion.value,
  "net.bytebuddy" % "byte-buddy" % "1.10.5",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
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
