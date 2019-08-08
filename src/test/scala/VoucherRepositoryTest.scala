import WebServer.Voucher
import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.{FunSuite, FunSuiteLike, Matchers}
import org.scalatest.time._

import scala.concurrent.duration._
import scala.concurrent.Await

class VoucherRepositoryTest extends TestKit(ActorSystem("MySpec")) with FunSuiteLike with Matchers with ScalaFutures {

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val patience = PatienceConfig(Span(1, Minute))

  test("testGetVoucher") {

    val repo: VoucherRepository = VoucherRepository()
    val result = repo.getVoucher(1)
    result.futureValue shouldBe None
  }

  test("testSaveGetVouchers") {

    val repo: VoucherRepository = VoucherRepository()
//    repo.getVouchers().futureValue shouldBe Seq()

    repo.saveVoucher(Voucher(1)).futureValue
    repo.saveVoucher(Voucher(2)).futureValue

    repo.getVouchers.futureValue should contain allOf (Voucher(1), Voucher(2))

  }

  test("testSaveVoucher") {

    val repo: VoucherRepository = VoucherRepository()
    repo.getVoucher(1).futureValue shouldBe None

    val voucher: Voucher = Voucher(1)
    val result = repo.saveVoucher(voucher)
    result.futureValue shouldBe ()

    val finalResult = repo.getVoucher(1)
    finalResult.futureValue shouldBe Some(Voucher(1))

  }

  test("testDeleteVoucher") {

    val repo: VoucherRepository = VoucherRepository()

    val voucher: Voucher = Voucher(1)
    val result = repo.saveVoucher(voucher)
    result.futureValue shouldBe ()

    val finalResult = repo.getVoucher(1)
    finalResult.futureValue shouldBe Some(Voucher(1))

    repo.deleteVoucher(1).futureValue shouldBe Done
  }

  test("fold") {

    val ints = Seq(1,2,3)

    ints.foldLeft(0)((acc, i) => acc + i)


  }

}
