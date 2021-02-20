/*
I want to try the construction of new ratings from implicit ratings through twitter.
Also I want to connect to another system where the ratings are explicit
Also Users and Items need to be indexed through unique keys for this example(not needed)
*/

package org.apache.spark.examples.mllib

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import breeze.signal.OptWindowFunction.User

object RecommendationExample {
  def fakemain(args: Array[String]): Unit = {
    //val spark = SparkSession.builder.appName("Project Text Mining").master("local").getOrCreate()
    val conf = new SparkConf().setAppName("CollaborativeFilteringExample").setMaster("local[*]")
    val sc = new SparkContext(conf)
    // $example on$
    // Load and parse the data
    val data = sc.textFile("User_Ratings.data")
    val ratings = data.map(_.split(',') match { case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10     //number of dimensions of features
    val numIterations = 10    //number of iterations of the algorithm, in most cases the model reach convergence in 20 iterations
    val model = ALS.train(ratings, rank, numIterations, 0.01)     //creation of the model

    // Evaluate the model on rating data
    val usersProducts = ratings.map { case Rating(user, product, rate) =>
      (user, product)
    }
    val predictions =
      model.predict(usersProducts).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }
    val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = (r1 - r2)
      err * err
    }.mean()
    println(s"Mean Squared Error = $MSE")

    // Save and load model
    //model.save(sc, "target/tmp/myCollaborativeFilter")
    val sameModel = MatrixFactorizationModel.load(sc, "target/tmp/myCollaborativeFilter")
    //var users= 
    sc.stop()
  }
}
