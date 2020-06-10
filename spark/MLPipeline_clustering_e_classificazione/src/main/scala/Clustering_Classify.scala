import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
import org.apache.spark.sql.Dataset
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator

object Clustering_Classify {

  def createPipelineKmeans(numcluster:Int){
    val tokenizer = new Tokenizer().setInputCol("readme").setOutputCol("words")
    val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
    val kmeans = new KMeans().setK(numcluster).setSeed(1L)
    new Pipeline().setStages(Array(tokenizer, hashingTF, kmeans))
  }
  def main(args: Array[String]) {
    val spark = SparkSession
        .builder
        .appName("Clustering and classification pipelines")
        .master("local")
        .getOrCreate()
    //TODO Data ingestion e streaming delle repository
    // keep attention on the types returned
    var readme: Dataset[Row]= spark.read.json("/home/josura/Desktop/AISuggestU/spark/MLPipeline_clustering_e_classificazione/fulldata.json")
    //readme = readme.dropDuplicates("owner")
    readme= readme.na.drop
    readme = readme.filter(length(readme("readme"))>3)
    val readmeDS=readme.as[RepoTyped]
    var numcluster :Int = 10
    var numiter:Int=10
    val lda = new LDA().setK(numcluster).setMaxIter(numiter)
    val model = lda.fit(dataset)
    // Prepare training repos from a list of (id, text-readme, label) tuples.
    //labels are deducted from previous clustering

    //TODO Decision of the model, probably based on the decisions of layer before in the pipeline
    val pipeline = createPipelineKmeans(10)

    val model = pipeline fit readme
    val predictions = model transform readme
  }
}
