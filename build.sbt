ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "lettuce-reproducer"
  )

libraryDependencies ++= Seq(
  "io.lettuce" % "lettuce-core" % "6.2.6.RELEASE",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
)