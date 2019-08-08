import WebServer.Voucher

import scala.concurrent.Future

/*
 * Adds a pointless level of indirection
 */
class VoucherService(val voucherRepository: VoucherRepository) {
  def getVouchers(): Future[Seq[Voucher]] = voucherRepository.getVouchers()
}
