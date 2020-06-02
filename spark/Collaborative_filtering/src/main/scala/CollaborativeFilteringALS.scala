
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.SparkSession

object CollaborativeFilteringALS {
    //timestamp not needed
  case class Rating(userId: Int, repositorieId: Int, rating: Float)
  def parseRating(str: String): Rating = {
    val fields = str.split(",|::|\\s")
    assert(fields.size == 3)
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toFloat)
  }

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("Collaborative Filtering ALS")
      .master("local")
      .getOrCreate()
    try {
      import spark.implicits._
      spark.sparkContext.setLogLevel("WARN")
      //TODO take ratings from site or from User_Ratings, User_Ratings should be a sink, or a target for kafka streaming
      val ratings = spark.read.textFile("User_Ratings.data")
        .map(parseRating)
        .toDF()
      val Array(training, test) = ratings.randomSplit(Array(0.8, 0.2))//test set and training set

      // Build the recommendation model using ALS on the training data
      val als = new ALS()
        .setMaxIter(5)
        .setRegParam(0.01)
        .setUserCol("userId")
        .setItemCol("repositorieId")
        .setRatingCol("rating")
      val model = als.fit(training)

      // Evaluate the model by computing the RMSE on the test data
      // Note we set cold start strategy to 'drop' to ensure we don't get NaN evaluation metrics
      model.setColdStartStrategy("drop")
      val predictions = model.transform(test)

      val evaluator = new RegressionEvaluator()
        .setMetricName("rmse")
        .setLabelCol("rating")
        .setPredictionCol("prediction")
      val rmse = evaluator.evaluate(predictions)
      println(s"Root-mean-square error = $rmse")

      // Generate top 10 repo recommendations for each user
      val userRecs = model.recommendForAllUsers(10)
      // Generate top 10 user recommendations for each repo
      val repoRecs = model.recommendForAllItems(10)

      // Generate top 10 repo recommendations for a specified set of users
      val users = ratings.select(als.getUserCol).distinct().limit(3)
      val userSubsetRecs = model.recommendForUserSubset(users, 10)
      // Generate top 10 user recommendations for a specified set of repos
      val repos = ratings.select(als.getItemCol).distinct().limit(3)
      val repoSubSetRecs = model.recommendForItemSubset(repos, 10)
      // $example off$
      userRecs.show()
      repoRecs.show()
      userSubsetRecs.show()
      repoSubSetRecs.show()
      //TODO return the repositories
    } catch {
      case e:Exception => println("generic error");spark.stop();
    }
    spark.stop()
  }
}
