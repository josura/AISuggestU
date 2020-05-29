//import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.SparkConf


object Main  {
  def main(args: Array[String]) {
    val logFile = "../../README.md" 

    def conf = new SparkConf().setAppName(appName).setMaster(master)
    def sc =  new SparkContext(conf)
    val logData = sc.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")

  /*
    val spark = SparkSession.builder.appName("Project Text Mining").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()*/
  }
}