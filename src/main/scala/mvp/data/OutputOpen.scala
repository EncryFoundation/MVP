package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.ECDSAUtils

//nonce - magic number
class OutputOpen extends MonetaryOutput {

  override def unlock(proofs: Seq[ByteString]): Boolean = true

  override val amount: Long = 5000L
  override val nonce: Long = 123L
  override val messageToSign: ByteString = Sha256RipeMD160(ByteString(amount) ++ ByteString(nonce))

  override val canBeSpent: Boolean = true

  override val id: ByteString = Sha256RipeMD160(ByteString(amount) ++ ByteString(nonce))
}
