package mvp.data

import akka.util.ByteString
import mvp.crypto.ECDSA
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.BlockchainUtils._
import mvp.utils.ECDSAUtils._

case class OutputAmount(address: ByteString,
                        amount: Long,
                        nonce: Long,
                        override val canBeSpent: Boolean = true)
  extends MonetaryOutput with AddressContainable {

  override val messageToSign: ByteString = Sha256RipeMD160(
    address ++ toByteString(amount) ++ toByteString(nonce)
  )

  //First proof - signature, second - publicKey, third - tx.bytes
  override def unlock(proofs: Seq[ByteString]): Boolean =
    ECDSA.verify(proofs.head, proofs.last, str2PublicKey(proofs(1))) &&
      address == Sha256RipeMD160(proofs(1))

  override val id: ByteString = Sha256RipeMD160(
    address ++ toByteString(amount) ++ toByteString(nonce)
  )
}

object OutputAmount {
  val typeId: Byte = 0: Byte
}
