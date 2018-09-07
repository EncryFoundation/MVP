package mvp.data

import java.security.PublicKey
import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class OutputPKI(bundle: ByteString,
                     check: ByteString,
                     publicKeyHash: ByteString,
                     userData: ByteString,
                     publicKey: PublicKey,
                     signature: ByteString,
                     nonce: Long,
                     override val canBeSpent: Boolean = true)
  extends Output
    with PublicKeyContainable
    with CloseableOutput {

  override val id: ByteString = Sha256RipeMD160(
    bundle
      ++ check
      ++ publicKeyHash
      ++ userData
      ++ ByteString(publicKey.getEncoded)
      ++ signature
      ++ ByteString(nonce)
  )

  override val messageToSign: ByteString = Sha256RipeMD160(
    bundle
      ++ check
      ++ publicKeyHash
      ++ userData
      ++ ByteString(publicKey.getEncoded)
      ++ ByteString(nonce)
  )

  override def unlock(proofs: Seq[ByteString]): Boolean = ???

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputPKI {
  val typeId: Byte = 1: Byte
}
