import org.apache.spark.sql.{Row,Dataset}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.length
//per convertire colonne in json in dataframe
import org.apache.spark.sql.functions._
//per la creazione di schemi
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType



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
//random forest
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{GBTClassificationModel, GBTClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
//naive bayes
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.classification.NaiveBayesModel



object Clustering_Classify {
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
  def createPipelineRandomForest(data:Dataset[Row]):Pipeline={
    val labelIndexer = new StringIndexer().setInputCol("label").setOutputCol("indexedLabel").fit(data)
    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 4 distinct values are treated as continuous.
    val featureIndexer = new VectorIndexer().setInputCol("features").setOutputCol("indexedFeatures").setMaxCategories(4).fit(data)
    // Train a GBT model.
    val gbt = new GBTClassifier().setLabelCol("indexedLabel").setFeaturesCol("indexedFeatures").setMaxIter(10).setFeatureSubsetStrategy("auto")
    // Convert indexed labels back to original labels.
    val labelConverter = new IndexToString().setInputCol("prediction").setOutputCol("predictedLabel").setLabels(labelIndexer.labels)
    // Chain indexers and GBT in a Pipeline.
    return new Pipeline().setStages(Array(labelIndexer, featureIndexer, gbt, labelConverter))

  }

  def predictNewReposLabel(oldRepos:Dataset[Row],newRepos:Dataset[Row],model:PipelineModel):Dataset[Row] = {
    val pipelineTokenizer = createPipelineTokenizer() 
    val tokenizer = pipelineTokenizer fit (oldRepos union newRepos)
    val newReposWithFeatures = pipelineTokenizer.fit(oldRepos union newRepos) transform newRepos
    return model transform newReposWithFeatures
  }
  def notmain(args: Array[String]) {
    val spark = SparkSession.builder.appName("Clustering and classification pipelines").master("local").getOrCreate()
    //TODO Data ingestion e streaming delle repository
    // keep attention on the types returned
    spark.sparkContext.setLogLevel("ERROR")
    val readme: Dataset[Row]= spark.read.json("fulldata.json")
    //readme = readme.dropDuplicates("owner")
    val readmeClean = cleanRepos(readme)
    //case class RepoTyped(url:String,owner:String,readme:String)
    //val readmeDS=readme.as[RepoTyped]
    // Prepare training repos from a list of (id, text-readme, label) tuples.
    //labels are deducted from previous clustering

    //TODO Decision of the model, probably based on the decisions of layer before in the pipeline
    val pipeline = createPipelineKmeans(10)

    val model = pipeline fit readmeClean
    val predictions = (model transform readmeClean).withColumnRenamed("prediction","label")
    //TODO streaming o simil streaming(with kafka), for now simulating with local data
    //val DailyReadme: Dataset[Row]= spark.read.json("/home/josura/Desktop/AISuggestU/spark/MLPipeline_clustering_e_classificazione/daily-fulldata.json")
    //val DailyReadmeClean = cleanRepos(DailyReadme)
    
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
    var newpredictions:Dataset[Row]=predictNewReposLabel(readmeClean,reposDaily,bayesmodel)
    //caricamento su elastic search
    val query = predictions.writeStream 
      .outputMode("append") 
      .queryName("writing_to_es") 
      .format("org.elasticsearch.spark.sql") 
      .option("checkpointLocation", "/tmp/") 
      .option("es.resource", "index/type") 
      .option("es.nodes", "elastic-search") 
      .start()

    query.awaitTermination()
  }
}