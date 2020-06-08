import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row

object Clustering_Classify {
  def main(args: Array[String]) {
    val spark = SparkSession
        .builder
        .appName("Clustering and classification pipelines")
        .master("local")
        .getOrCreate()
    //TODO Data ingestion e streaming delle repository

    val tokenizer = new Tokenizer()
        .setInputCol("text")
        .setOutputCol("words")
    val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")
    val lda = new LDA().setK(10).setMaxIter(10)
        .setInputCol(hashingTF.getOutputCol)
        .setOutputCol("Cluster")

    //TODO Decision of the model, probably based on the decisions of layer before in the pipeline
    val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, lda))
  }
}
