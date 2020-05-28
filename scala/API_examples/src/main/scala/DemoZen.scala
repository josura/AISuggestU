import github.features.Zen
import scala.collection.JavaConverters._

object DemoZen extends App {
  /**
    * Display Zen of GitHub
    */
  val gitHubCli = new github.Client(
    "https://api.github.com"
    , System.getenv().asScala ("TOKEN_GITHUB_DOT_COM") //sys.env("TOKEN_GITHUB_DOT_COM")
  ) with Zen

  gitHubCli.octocatMessage().fold(
    {errorMessage => println(s"Error: $errorMessage")},
    {data => println(data)}
  )

}