import WebServer.Voucher
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.{DynamoClient, DynamoSettings}
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import org.scanamo._
import org.scanamo.error.DynamoReadError
import org.scanamo.ops.AlpakkaInterpreter.Alpakka
import org.scanamo.syntax._
import org.scanamo.auto._

import scala.collection.immutable
import scala.concurrent.Future

case class VoucherRepository (
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer
) {
  private val voucherTable = Table[Voucher]("Vouchers")

  private val scanamoClient = ScanamoAlpakka(DynamoClient(DynamoSettings(system)))

  def getVouchers(): Future[Seq[Voucher]] = {
    val resultStream: Alpakka[List[Either[DynamoReadError, Voucher]]] = scanamoClient.exec(voucherTable.scan())
    val mappedResult = resultStream
      .map((list: immutable.Seq[Either[DynamoReadError, Voucher]]) => {
        list.map((item: Either[DynamoReadError, Voucher]) => {
          item match {
            case Left(err) => throw new RuntimeException(err.toString)
            case Right(voucher) => voucher
          }
        })
      })
      .runFold(Seq[Voucher]())(_ ++ _)

    mappedResult
  }

  def getVoucher(id: Int): Future[Option[Voucher]] = {
    val resultStream: Alpakka[Option[Either[DynamoReadError, Voucher]]] = scanamoClient.exec(voucherTable.get('id -> id))
    val mapResult: Source[Option[Voucher], NotUsed] = resultStream.map {
      case None => None
      case Some(Right(voucher)) => Some(voucher)
      case Some(Left(err)) => throw new RuntimeException(err.toString)
    }
    val runResult: Future[Option[Voucher]] = mapResult.runWith(Sink.head)

    runResult
  }

  def saveVoucher(voucher: Voucher): Future[Done] = {
    val resultStream: Alpakka[Option[Either[DynamoReadError, Voucher]]] = scanamoClient.exec(voucherTable.put(voucher))
    val mapResult: Source[Done, NotUsed] = resultStream.map((item: Option[Either[DynamoReadError, Voucher]]) => {
      item match {
        case Some(Left(err)) => throw new RuntimeException(err.toString)
        case _ => Done
      }
    })
    val runResult: Future[Done] = mapResult.runWith(Sink.head)

    runResult
  }

  def deleteVoucher(id: Int): Future[Done] = {
    val resultStream: Alpakka[DeleteItemResult] = scanamoClient.exec(voucherTable.delete('id -> id))
    val mapResult = resultStream.map(item => Done)
    mapResult.runWith(Sink.head)
  }
}
