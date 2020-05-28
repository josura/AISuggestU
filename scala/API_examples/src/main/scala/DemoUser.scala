import github.features.Users
import scala.collection.JavaConverters._

/**
  * Display user informations on GitHub
  */
  
object DemoUser  extends App {

  val gitHubCli = new github.Client(
    "https://api.github.com",
    System.getenv().asScala ("TOKEN_GITHUB_DOT_COM") //sys.env("TOKEN_GITHUB_DOT_COM")
  ) with Users


  var giorgio = gitHubCli.fetchUser("josura")/*.fold(
    {errorMessage => println(errorMessage)},
    {userInformation:Option[Any] =>
      println(
        userInformation
          .map(user => user.asInstanceOf[Map[String, Any]])
          .getOrElse("Huston? We've got a problem!")
      )
    }
  )*/

  println(giorgio)

}