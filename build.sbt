name := "Uberv2"

version := "0.2"

scalaVersion := "2.12.8" // don't use 2.12.3, throw Exceptions
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0"// % "runtime",

// libraryDependencies += "org.apache.spark" %% "spark-mllib-local" % "3.0.0"

resolvers += "Cloudera" at "http://repository.cloudera.com/artifactory/cloudera-repos/"
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
mainClass in compile := Some("UberData")
//src.main.scala can be excluded from package name, after that everything should be included
