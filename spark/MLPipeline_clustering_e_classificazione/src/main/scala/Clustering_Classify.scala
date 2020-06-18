import org.apache.spark.sql.{Row,Dataset}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.length
//per convertire colonne in json in dataframe
import org.apache.spark.sql.functions._
//per la creazione di schemi
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType

//elastic
import org.elasticsearch.spark.sql._

//naive bayes
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.classification.NaiveBayesModel

//kafka streaming
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

//pipeline e clstering
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.ml.linalg.Vector

import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator





object Clustering_Classify {
  case class repositorieClassified(url:String,owner:String,label:Int)
  def cleanRepos(readme:Dataset[Row]):Dataset[Row] = {
    return readme.na.drop.filter(length(readme("readme"))>3)
  }
  def createPipelineTokenizer() :Pipeline = {
    val tokenizer = new Tokenizer().setInputCol("readme").setOutputCol("words")
    val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
    new Pipeline().setStages(Array(tokenizer, hashingTF))
  }
  def createPipelineKmeans(numcluster:Int):Pipeline = {
    val tokenizer = new Tokenizer().setInputCol("readme").setOutputCol("words")
    val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
    val kmeans = new KMeans().setK(numcluster).setSeed(1L)
    new Pipeline().setStages(Array(tokenizer, hashingTF, kmeans))
  }
  // specify layers for the neural network:
// (input layer of size 4 (features), intermediate layers, output of the labelsvo)
//not until a lot of records, I can also get low with the number of perceptrons in a layer, TODO in future
  def createPipelinePerceptrons(layers:Array[Int]) :Pipeline = {
    val tokenizer = new Tokenizer().setInputCol("readme").setOutputCol("words")
    val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
    val trainer = new MultilayerPerceptronClassifier().setLayers(layers).setBlockSize(128).setSeed(1234L).setMaxIter(100)
    new Pipeline().setStages(Array(tokenizer, hashingTF, trainer))
  }
  

  def predictNewReposLabel(oldRepos:Dataset[Row],newRepos:Dataset[Row],model:PipelineModel):Dataset[Row] = {
    val pipelineTokenizer = createPipelineTokenizer() 
    val tokenizer = pipelineTokenizer fit (oldRepos union newRepos)
    val newReposWithFeatures = pipelineTokenizer.fit(oldRepos union newRepos) transform newRepos
    return model transform newReposWithFeatures
  }
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Clustering and classification pipelines").
    config("es.nodes","elastic-search").config("es.index.auto.create", "true").config("es.port","9200").//config("es.nodes.wan.only", "true").
    master("local[*]").getOrCreate()
    //TODO Data ingestion e streaming delle repository
    // keep attention on the types returned
    spark.sparkContext.setLogLevel("ERROR")
    val readme: Dataset[Row]=  cleanRepos(spark.read.json("data/fulldata.json"))
    //readme = readme.dropDuplicates("owner")
    //case class RepoTyped(url:String,owner:String,readme:String)
    //val readmeDS=readme.as[RepoTyped]
    // Prepare training repos from a list of (id, text-readme, label) tuples.
    //labels are deducted from previous clustering

    //TODO Decision of the model, probably based on the decisions of layer before in the pipeline
    val pipeline = createPipelineKmeans(10)

    val model = pipeline fit readme
    val predictions = (model transform readme).withColumnRenamed("prediction","label")
    
    /*try {
      // Create context with 2 second batch interval
      val streamingContext = new StreamingContext(spark.sparkContext, Seconds(5))
      val kafkaParams = Map[String, Object](
        "bootstrap.servers" -> "kafka:9092",
        "key.deserializer" -> classOf[StringDeserializer],
        "value.deserializer" -> classOf[StringDeserializer],
        "group.id" -> "dailyrepo_group",
        "auto.offset.reset" -> "latest",
        "enable.auto.commit" -> (false: java.lang.Boolean)
      )
      
      val topics = Array("daily-repos")
      val stream = KafkaUtils.createDirectStream[String, String](
        streamingContext,
        PreferConsistent,
        Subscribe[String, String](topics, kafkaParams)
      )

      val messageStreaming = stream.map(record => (record.value))

      import spark.implicits._

      //val messageStreamingDF = messageStreaming.map(rdd=>rdd.map(recordstr=>spark.read.json(Seq(recordstr.toString()).toDS)))
      val messageStreamingDF = messageStreaming.map(stringa=>spark.read.json(Seq(stringa).toDS))

      messageStreamingDF.foreachRDD(rdd=> rdd.foreach(_.show))
      streamingContext.start()
      streamingContext.awaitTermination()
    } catch {
      case e:Exception=>{spark.stop()}
    }*/
    //creazione modello classificazione
    val bayesPipeline = new Pipeline().setStages( Array(new NaiveBayes))

    val bayesmodel = bayesPipeline fit predictions

    

    val fulldf = spark.readStream.format("kafka").option("kafka.bootstrap.servers","kafka:9092").option("subscribe","daily-repos").load()
    val repoStringDF = fulldf.selectExpr("CAST(value AS STRING)")
    val schemaRepo = new StructType().add("url",StringType).add("owner",StringType).add("readme",StringType)
    val reposDaily=repoStringDF.select(from_json(col("value"),schemaRepo).as("data")).select("data.*")
    
    

    //da modificare
    val newpredictions:Dataset[Row]=predictNewReposLabel(readme,reposDaily,bayesmodel)

    //newpredictions.writeStream.format("console").outputMode("append").start().awaitTermination()
    //caricamento su elastic search
    /*val query = newpredictions.writeStream .outputMode("append") .queryName("writing_to_es")  .format("org.elasticsearch.spark.sql").option("checkpointLocation", "/tmp/").option("es.resource", "repository/classified").option("es.nodes", "elastic-search:9200") 
      .start()
    query.awaitTermination()*/
    import org.elasticsearch.spark.sql._
    import spark.implicits._

    val repositoriesTyped = newpredictions.select(col("url"),col("owner"),col("prediction").cast(IntegerType).as("label")).as[repositorieClassified]
    repositoriesTyped.writeStream.outputMode("append").format("es").option("checkpointLocation","/tmp").option("es.mapping.id","url").start("repositories/classified").awaitTermination

  }
}