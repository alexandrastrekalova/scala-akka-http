import akka.Done
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

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn

object WebServer {

  def sayHelloName(name: String): String = {
    s"<h1>Hello ${name}</h1>"
  }

  def route(repository: VoucherRepository)(implicit ec: ExecutionContext): Route = concat(helloRoute, voucherRoute(repository))

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

  def voucherRoute(repository: VoucherRepository)(implicit ec: ExecutionContext): Route =
    pathPrefix("vouchers") {
      get {
        complete(repository.getVouchers)
      } ~
      path("batch") {
        post {
          entity(as[VoucherBatchRequest]) { voucherBatchRequest =>
            val eventualUnits: immutable.Seq[Future[Done]] = (1 to voucherBatchRequest.quantity).map(i => {
              repository.saveVoucher(Voucher(i))
            })
            val functionToEventualUnit: Future[Seq[Done]] = Future.sequence(eventualUnits)

            onSuccess(functionToEventualUnit) { _: Seq[Done] =>
              complete(StatusCodes.Created)
            }

          }
        }
      }
    }

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val repository: VoucherRepository = VoucherRepository()

    val bindingFuture = Http().bindAndHandle(route(repository), "localhost", 8086)

    bindingFuture.foreach(b => {
      println(s"Server online at http://${b.localAddress.getHostName}:${b.localAddress.getPort}/\nPress RETURN to stop...")
    })
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