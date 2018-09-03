package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class OutputAmount(publicKey: ByteString,
                        amount: Long,
                        signature: ByteString,
                        override val canBeSpent: Boolean = true) extends Output {

  override def closeForSpent: Output = this.copy(canBeSpent = false)

  override val messageToSign: ByteString = Sha256RipeMD160(publicKey ++ toByteString(amount))

  override def unlock(proofs: Seq[ByteString]): Boolean = true

  override val id: ByteString = Sha256RipeMD160(publicKey ++ toByteString(amount))
}

object OutputAmount {
  val typeId: Byte = 0: Byte
}
