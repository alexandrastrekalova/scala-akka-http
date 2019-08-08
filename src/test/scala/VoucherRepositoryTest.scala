import WebServer.Voucher
import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Span, _}
import org.scalatest.{BeforeAndAfter, FunSuiteLike, Matchers}

class VoucherRepositoryTest extends TestKit(ActorSystem("VoucherRepositoryTest"))
  with FunSuiteLike
  with BeforeAndAfter
  with Matchers
  with ScalaFutures {

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val patience: PatienceConfig = PatienceConfig(Span(1, Minute))

  var repo: VoucherRepository = _

  before {
    repo = VoucherRepository()
    repo.deleteVoucher(1).futureValue
    repo.deleteVoucher(2).futureValue
    repo.getVouchers().futureValue shouldBe Seq()
    println("Repo is empty")

  }

  test("testGetVoucher") {

    val result = repo.getVoucher(1)
    result.futureValue shouldBe None
  }

  test("testSaveGetVouchers") {

    repo.getVouchers().futureValue shouldBe Seq()

    repo.saveVoucher(Voucher(1)).futureValue
    repo.saveVoucher(Voucher(2)).futureValue

    repo.getVouchers().futureValue should contain allOf (Voucher(1), Voucher(2))

  }

  test("testSaveVoucher") {

    repo.getVoucher(1).futureValue shouldBe None

    val voucher: Voucher = Voucher(1)
    val result = repo.saveVoucher(voucher)
    result.futureValue shouldBe Done

    val finalResult = repo.getVoucher(1)
    finalResult.futureValue shouldBe Some(Voucher(1))

  }

  test("testDeleteVoucher") {

    val voucher: Voucher = Voucher(1)
    val result = repo.saveVoucher(voucher)
    result.futureValue shouldBe Done

    val finalResult = repo.getVoucher(1)
    finalResult.futureValue shouldBe Some(Voucher(1))

    repo.deleteVoucher(1).futureValue shouldBe Done
  }
}
