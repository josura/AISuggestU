import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
import org.apache.spark.sql.Dataset

object Clustering_Classify {
  def main(args: Array[String]) {
    val spark = SparkSession
        .builder
        .appName("Clustering and classification pipelines")
        .master("local")
        .getOrCreate()
    //TODO Data ingestion e streaming delle repository

    var numcluster :Int = 10
    var numiter:Int=10
    val lda = new LDA().setK(numcluster).setMaxIter(numiter)
    val model = lda.fit(dataset)
    // Prepare training repos from a list of (id, text-readme, label) tuples.
    //labels are deducted from previous clustering

    val tokenizer = new Tokenizer()
        .setInputCol("text")
        .setOutputCol("words")
    val hashingTF = new HashingTF()
        .setNumFeatures(1000)
        .setInputCol(tokenizer.getOutputCol)
        .setOutputCol("features")

    //TODO Decision of the model, probably based on the decisions of layer before in the pipeline
    val pipeline = new Pipeline()
        .setStages(Array(tokenizer, hashingTF, lda))

    val model = pipeline.fit()//dati
  }
}
