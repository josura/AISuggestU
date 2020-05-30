import org.apache.spark.sql._
import org.apache.spark.sql.types._

trait TextMiningInterface {
  val spark: SparkSession
}
