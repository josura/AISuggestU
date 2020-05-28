import github.features.Repositories
import scala.collection.JavaConverters._

/**
  * Create a repository
  */

object DemoRepositories  extends App {

  val gitHubCli = new github.Client(
    "https://api.github.com",
    System.getenv().asScala ("TOKEN_GITHUB_DOT_COM") //sys.env("TOKEN_GITHUB_DOT_COM")
  ) with Repositories

  gitHubCli.createRepository(
    name = "hello_earth_africa",
    description = "Hello world :heart:",
    isPrivate = false,
    hasIssues = true
  ).fold(
    {errorMessage => println(s"Error: $errorMessage")},
    {repositoryInformation:Option[Any] =>
      println(
        repositoryInformation
          .map(repo => repo.asInstanceOf[Map[String, Any]])
          .getOrElse("ouch!")
      )
    }
  )
}