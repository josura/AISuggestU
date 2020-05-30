import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD


object Main  {
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

    val word2vec = new Word2Vec()

    val model = word2vec.fit(seqWords)

    val synonyms = model.findSynonyms("scala", 5)
    println("word2vec")
    for((synonym, cosineSimilarity) <- synonyms) {
      println(s"$synonym $cosineSimilarity")
    }


    spark.stop()
  }
}