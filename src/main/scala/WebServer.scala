import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object WebServer {

  def sayHelloName(name: String): String = {
    s"<h1>Hello ${name}</h1>"
  }

  def route: Route = concat(helloRoute, voucherRoute)

  val helloRoute: Route =
  path("hello") {
    get {
      val value = "name".?("stranger")
      parameter(value) { name =>
        complete(sayHelloName(name))
      }
    }
  }

  val getVouchers: Seq[Voucher] = Seq(Voucher(1), Voucher(2))

  implicit val voucherFormat: RootJsonFormat[Voucher] = jsonFormat1(Voucher)
  implicit val voucherBatchRequestFormat = jsonFormat3(VoucherBatchRequest)

  var vouchers: Seq[Voucher] = Seq()

  val voucherRoute: Route =
    pathPrefix("vouchers") {
      get {
        complete(vouchers)
      } ~
      path("batch") {
        post {
          entity(as[VoucherBatchRequest]) { voucherBatchRequest =>
            println(voucherBatchRequest)
            (1 to voucherBatchRequest.quantity).foreach(i => {
              vouchers = vouchers.appended( Voucher(i))
            })
            complete(StatusCodes.Created)
          }
        }
      }
    }

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  private def sayHello() = {
    HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
  }

  private def sayHelloString = {
    "<h1>Say hello to akka-http</h1>"
  }

  private def sayHelloFuture: Future[String] = {
    Future.failed(new RuntimeException("Something went wrong..."))
  }

  case class Voucher(id: Int)
  case class VoucherBatchRequest(price: Int, quantity: Int, description: String)

}