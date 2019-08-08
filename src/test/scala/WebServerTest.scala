import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Minute, Span}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class WebServerTest extends FunSuite
  with BeforeAndAfter
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with SprayJsonSupport {

  implicit val patience: PatienceConfig = PatienceConfig(Span(1, Minute))

  var repo: VoucherRepository = _

  before {
    repo = VoucherRepository()
    repo.deleteVoucher(1).futureValue
    repo.deleteVoucher(2).futureValue
    repo.getVouchers().futureValue shouldBe Seq()
  }

  test("should test route") {
    Get("/hello") ~> WebServer.route(repo) ~> check {
      responseAs[String] shouldEqual "<h1>Hello stranger</h1>"
    }
  }

  test("should return an empty array") {
    Get("/vouchers") ~> WebServer.route(repo) ~> check {
      responseAs[String] shouldEqual """[]"""
    }
  }

  test("should create 2 vouchers") {
    // using the RequestBuilding DSL:

    val body = """{"price":10,"quantity":2,"description":"unit test"}"""

    Post("/vouchers/batch")
      .withEntity(ContentTypes.`application/json`, body) ~> WebServer.route(repo) ~> check {
      status shouldBe StatusCodes.Created
    }
  }

  test("should create and return 2 vouchers") {
    // using the RequestBuilding DSL:

    val body = """{"price":10,"quantity":2,"description":"unit test"}"""

    Post("/vouchers/batch")
      .withEntity(ContentTypes.`application/json`, body) ~> WebServer.route(repo) ~> check {
      Get("/vouchers") ~> WebServer.route(repo) ~> check {
        responseAs[String].length shouldEqual 19  // """[{"id":1},{"id":2}]""", order ignored
      }
    }
  }

}
