name := "Collaborative Filtering"

version := "0.1"

scalaVersion := "2.11.11"

scalacOptions ++= Seq("-language:implicitConversions", "-deprecation")
libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % Test,
  ("org.apache.spark" %% "spark-core" % "2.4.5"),
  ("org.apache.spark" %% "spark-mllib" % "2.4.5"),
  ("org.apache.spark" %% "spark-sql" % "2.4.5"),
  //("org.apache.kafka" %% "kafka-streams-scala" % "2.5.0"),
  ("org.apache.kafka" %% "kafka-streams-scala" % "2.4.1"),
  ("org.apache.kafka" % "kafka-clients" % "2.5.0"),
  ("org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5"),
  ("org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.5"),
  //("org.elasticsearch" % "elasticsearch-hadoop" % "7.7.1")
  ("org.elasticsearch" % "elasticsearch-spark-20_2.11" % "7.7.1"),
  ("org.apache.spark" %% "spark-avro" % "2.4.5")
)

dependencyOverrides ++= {
  Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
  )
}