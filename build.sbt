name := "spark-mappartition-actor"

version := "0.1"

scalaVersion := "2.12.10"

lazy val akkaVersion = "2.6.13"
lazy val sparkVersion = "3.0.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

/** --------------------------------------- */
/** Required for operation of Apache Spark. */
/** --------------------------------------- */

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % Provided

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % Provided

/** ------------------------- */
/** Required for application. */
/** ------------------------- */

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
