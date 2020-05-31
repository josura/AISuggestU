import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.sql.types.{ArrayType, IntegerType, MapType, StringType, StructType}
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import java.util.Base64


object Main  {
  def text2BinaryDecoding(encoded: String): String = {
    val decoded = Base64.getDecoder.decode(encoded)
    new String(decoded, "UTF-8")
  }

  def main(args: Array[String]) {
    val logFile = "README.md"
    def commonwords:Array[String] = Array("a","the","an","have","had","and","you","it","or","at")
    //def conf = new SparkConf().setAppName("Main Text Mining").setMaster("local")
    /*    def sc =  new SparkContext(conf)
        val logData = sc.textFile(logFile).cache()
        val numAs = logData.filter(line => line.contains("a")).count()
        val numBs = logData.filter(line => line.contains("b")).count()
        println(s"Lines with a: $numAs, Lines with b: $numBs")
    */
    val spark = SparkSession.builder.appName("Project Text Mining").master("local").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numAs = logData.filter(line => line.split(" ").contains("a")).count()
    val seqWords: RDD[Seq[String]] = logData.rdd.map(word =>word.toLowerCase.split(" ").filter(word=> !commonwords.contains(word)).toSeq)


      //flatMap(line => line.split(" ") ).foreach(println).groupByKey().count()
    println(s"Lines with a: $numAs")

    val hashingTF = new HashingTF()
    val tf: RDD[Vector] = hashingTF.transform(seqWords)

    // While applying HashingTF only needs a single pass to the data, applying IDF needs two passes:
    // First to compute the IDF vector and second to scale the term frequencies by IDF.
    tf.cache()
    val idf = new IDF().fit(tf)
    val tfidf: RDD[Vector] = idf.transform(tf)

    println("tfidf: ")
    tfidf.foreach(x => println(x))

    val word2Vec = new Word2Vec()
      .setInputCol("text")
      .setOutputCol("result")
      .setVectorSize(3)
      .setMinCount(0)

    /*val model = word2vec.fit(seqWords)

    val synonyms = model.findSynonyms("scala", 5)
    println("word2vec")
    for((synonym, cosineSimilarity) <- synonyms) {
      println(s"$synonym $cosineSimilarity")
    }*/




    import scala.io.Source
    val html = Source.fromURL("https://api.github.com/repos/josura/AISuggestU/contents/README.md")
    val jsonString = html.mkString
    println(jsonString)

    import spark.implicits._

    val readmedf = spark.read.json(Seq(jsonString).toDS)
    readmedf.show()
    println("content:")
    val contentCode = readmedf.select("content").collect()(0)(0).toString    //the first collect return an array of rows, the members of rows
    println(contentCode)
    val readmeContent= text2BinaryDecoding(contentCode.replaceAll("\n",""))//map{ case '-' => '+'; case '_' => '/'; case c => c; case c => c })
    //val schemaCol = new StructType().add("content",StringType).add("plaincontent",StringType)
    try {
      val colCorrette = readmedf.select(col("content"),col("content").as("plaincontent"))
        .collect()
        .map(value =>Row(value(0),text2BinaryDecoding(value(1).toString.replaceAll("\n",""))))
        //.map(value => Row(value))
        .toSeq

    //val colDataf=colCorrette.toDF("plaincontent")
    val colDataf=spark.createDataFrame(spark.sparkContext.parallelize(colCorrette),schemaCol)
    colDataf.show()
    //var readmeDFPlain = readmedf.withColumn("plaincontent", colDataf("plaincontent"))
    var fullContentDF =  readmedf.join(colDataf,colDataf("content")===readmedf("content"))
    fullContentDF.show()
    } catch {
      case  e:Exception => {println("sto morendo"); spark.stop;}

    }
    //readmedf.withColumn("content",text2BinaryDecoding(col("content").toSring.replaceAll("\n","")))
    //readmedf.show()

    val word2Vec = new Word2Vec()
      .setInputCol("text")
      .setOutputCol("result")
      .setVectorSize(3)
      .setMinCount(0)

    val model = word2vec.fit( fullContentDF.select("plaincontent").collect()(0)(0).toString)

    spark.stop()

}
}