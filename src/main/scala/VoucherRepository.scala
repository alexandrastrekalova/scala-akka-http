import WebServer.Voucher
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.{DynamoClient, DynamoSettings}
import akka.stream.scaladsl.{Sink, Source}
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.auto._
import org.scanamo.error.DynamoReadError
import org.scanamo.ops.AlpakkaInterpreter.Alpakka

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

case class VoucherRepository (
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer
) {
  private val voucherTable = Table[Voucher]("Vouchers")

  private val scanamoClient = ScanamoAlpakka(DynamoClient(DynamoSettings(system)))

  def getVouchers(): Future[Seq[Voucher]] = {
    val resultStream: Alpakka[List[Either[DynamoReadError, Voucher]]] = scanamoClient.exec(voucherTable.scan())
    val flatten: (Seq[Voucher], immutable.Seq[Voucher]) => Seq[Voucher] = (acc, i) => acc ++ i
    val mappedResult = resultStream.map((list: immutable.Seq[Either[DynamoReadError, Voucher]]) => {
      list.map((item: Either[DynamoReadError, Voucher]) => {
        item match {
          case Left(err) => throw new RuntimeException(err.toString)
          case Right(voucher) => voucher
        }
      })
    })
        .runFold(Seq[Voucher]())(flatten)


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
}
