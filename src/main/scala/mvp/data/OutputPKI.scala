package mvp.data

import java.security.PublicKey

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160

case class OutputPKI(bundle: ByteString,
                     check: ByteString,
                     publicKeyHash: ByteString,
                     userData: ByteString,
                     publicKey: PublicKey,
                     signature: ByteString,
                     override val canBeSpent: Boolean = true) extends Output {

  override val id: ByteString = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ ByteString(publicKey.getEncoded) ++ signature
  )

  override val messageToSign: ByteString = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ ByteString(publicKey.getEncoded)
  )

  override def unlock(proofs: Seq[ByteString]): Boolean = ???

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputPKI {
  val typeId: Byte = 1: Byte
}
