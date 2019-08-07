import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}

class WebServerTest extends FunSuite with Matchers with ScalatestRouteTest with ScalaFutures with SprayJsonSupport{

  test("should test route") {
    Get("/hello") ~> WebServer.route ~> check {
      responseAs[String] shouldEqual "<h1>Hello stranger</h1>"
    }
  }

  test("should return a bunch of vouchers") {
    Get("/vouchers") ~> WebServer.route ~> check {
      responseAs[String] shouldEqual """[{"id":1},{"id":2}]"""
    }
  }

  test("should create a voucher batch") {
    // using the RequestBuilding DSL:

    val body = """{"price":10,"quantity":100,"description":"unit test"}"""

    Post("/vouchers/batch")
      .withEntity(ContentTypes.`application/json`, body) ~> WebServer.route ~> check {
      status shouldBe StatusCodes.Created
    }
  }

  test("should create and return 3 vouchers") {
    // using the RequestBuilding DSL:

    val body = """{"price":10,"quantity":3,"description":"unit test"}"""

    Post("/vouchers/batch")
      .withEntity(ContentTypes.`application/json`, body) ~> WebServer.route ~> check {
      Get("/vouchers") ~> WebServer.route ~> check {
        responseAs[String] shouldEqual """[{"id":1},{"id":2},{"id":3}]"""
      }
    }
  }

}
