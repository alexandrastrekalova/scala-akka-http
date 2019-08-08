import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}

class GithubClient(implicit system: ActorSystem, implicit val materializer: ActorMaterializer) {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val userFormat: RootJsonFormat[User] = jsonFormat1(User)

  def getUser(username: String): Future[Option[User]] = {
    Http().singleRequest(HttpRequest(uri = s"https://api.github.com/users/$username"))
      .flatMap {
        case response@HttpResponse(StatusCodes.OK, _, _, _) => Unmarshal(response).to[Option[User]]
        case HttpResponse(StatusCodes.NotFound, _, _, _) => Future.successful(None)
        case _ => Future.failed(new RuntimeException("Bad request"))
      }
  }
}

case class User(login: String)