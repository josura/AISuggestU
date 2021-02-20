import java.util.Base64

import org.apache.spark.sql._
import org.apache.spark.sql.types._

class textMining extends TextMiningInterface {
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.functions._
  def text2BinaryDecoding(encoded: String): String = {
    val decoded = Base64.getDecoder.decode(encoded)
    new String(decoded, "UTF-8")
  }
  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Time Usage")
      .master("local")
      .getOrCreate()

  // For implicit conversions like converting RDDs to DataFrames
  import spark.implicits._

  //def tfIDF(documentsSec :RDD[Seq[String]])
}
