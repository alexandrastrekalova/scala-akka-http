import WebServer.Voucher
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.mockito.IdiomaticMockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuiteLike, Matchers}

import scala.concurrent.Future

class VoucherServiceTest extends TestKit(ActorSystem("VoucherServiceTest"))
  with FunSuiteLike
  with IdiomaticMockito
  with Matchers
  with ScalaFutures {

  test("should return 2 vouchers") {
    val voucherRepositoryMock = mock[VoucherRepository]
    voucherRepositoryMock.getVouchers shouldReturn Future.successful(Seq(WebServer.Voucher(4711), WebServer.Voucher(101)))

    val voucherService = new VoucherService(voucherRepositoryMock)
    voucherService.getVouchers.futureValue should contain allOf (Voucher(101), Voucher(4711))
  }
}
