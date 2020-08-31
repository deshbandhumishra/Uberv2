import sbt.Keys.{libraryDependencies, scalaVersion}

version := "0.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.mcit",
      scalaVersion := "2.12.5" // don't use 2.12.3, throws Exceptions

    )),
    name := "Uberv2",

    resolvers ++= Seq (
      Opts.resolver.mavenLocalFile,
      "Confluent" at "http://packages.confluent.io/maven"
    ),


      libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.0.0",
      "org.apache.spark" %% "spark-sql" % "3.0.0",
      "org.apache.spark" %% "spark-mllib" % "3.0.0",
//============================================================
        "org.apache.kafka" % "kafka-clients" % "0.10.1.1",
            "org.apache.hadoop" % "hadoop-common" % "2.6.0",
           "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0",
           "org.apache.spark" %% "spark-streaming" % "2.4.0",
           "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0",
     //==============================================================

        "com.typesafe" % "config" % "1.3.2",
        "org.apache.kafka" % "kafka-clients" % "2.4.0",
        "org.apache.kafka" % "connect-json" % "2.4.0",
        "org.apache.kafka" % "connect-runtime" % "2.4.0",
        "org.apache.kafka" % "kafka-streams" % "2.4.0",
        "org.apache.kafka" %% "kafka-streams-scala" % "2.4.0",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.5",
        "org.apache.kafka" % "connect-runtime" % "2.1.0",
        "io.confluent" % "kafka-json-serializer" % "5.0.1",
        "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar")) // this is a workaround for https://github.com/jax-rs/api/issues/571
      )





  )




ThisBuild / useCoursier := false

// libraryDependencies += "org.apache.spark" %% "spark-mllib-local" % "3.0.0"

