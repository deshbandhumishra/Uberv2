import sbt.Keys.scalaVersion

version := "0.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.mcit",
      scalaVersion := "2.12.8" // don't use 2.12.3, throw Exceptions

    )),
    name := "Uberv2",
      libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.0.0",
      "org.apache.spark" %% "spark-sql" % "3.0.0",
      "org.apache.spark" %% "spark-mllib" % "3.0.0"

    )

  )

// libraryDependencies += "org.apache.spark" %% "spark-mllib-local" % "3.0.0"

resolvers += "Cloudera" at "http://repository.cloudera.com/artifactory/cloudera-repos/"

 
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
mainClass in compile := Some("com.mcit.UberData")
enablePlugins(AshScriptPlugin)
//src.main.scala can be excluded from package name, after that everything should be included
dockerBaseImage := "openjdk:jre-slim"