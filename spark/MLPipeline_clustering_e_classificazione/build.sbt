name := "Collaborative Filtering"

version := "0.1"

scalaVersion := "2.12.11"

scalacOptions ++= Seq("-language:implicitConversions", "-deprecation")
libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % Test,
  ("org.apache.spark" %% "spark-core" % "2.4.5"),
  ("org.apache.spark" %% "spark-mllib" % "2.4.5"),
  ("org.apache.spark" %% "spark-sql" % "2.4.5"),
  ("org.apache.kafka" %% "kafka-streams-scala" % "2.5.0"),
  ("org.apache.kafka" % "kafka-clients" % "2.5.0"),
  ("org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5"),
  ("org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.5")
)