import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.{FunSuiteLike, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Minute, Span}

class GithubClientTest extends TestKit(ActorSystem("MySpec")) with FunSuiteLike with Matchers with ScalaFutures{

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val patience: PatienceConfig = PatienceConfig(Span(1, Minute))

  test("should retrieve github user json"){

    val client = new GithubClient()

    val maybeUser = client.getUser("malikonjo").futureValue

    maybeUser.get.login shouldBe "malikonjo"
  }

  test("should handle non existent user"){

    val client = new GithubClient()

    val maybeUser = client.getUser("skljfhwlkef").futureValue

    maybeUser shouldBe None
  }

}
