package mvp.data

import akka.util.ByteString
import mvp.local.messageTransaction.MessageInfo
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.Base16.encode
import io.circe.syntax._
import io.circe.generic.auto._
import mvp.crypto.ECDSA
import mvp.utils.EncodingUtils._

case class OutputMessage(bundle: ByteString,
                         check: ByteString,
                         messageHash: ByteString,
                         metadata: ByteString,
                         publicKey: ByteString,
                         signature: ByteString,
                         txNum: Int,
                         nonce: Long,
                         override val canBeSpent: Boolean = true)
  extends Output
    with PublicKeyContainable
    with CloseableOutput {

  def toProofGenerator: MessageInfo = MessageInfo(messageHash, metadata, publicKey)

  override val id: ByteString = Sha256RipeMD160(
    bundle
      ++ check
      ++ messageHash
      ++ metadata
      ++ publicKey
      ++ signature
      ++ ByteString(nonce)
  )

  override val messageToSign: ByteString = Sha256RipeMD160(
    bundle
      ++ check
      ++ messageHash
      ++ metadata
      ++ publicKey
      ++ ByteString(nonce)
  )

  def checkSignature: Boolean = {
    val result: Boolean = ECDSA.verify(signature, messageToSign, publicKey)
    logger.info(s"Going to check signature for output with id: ${encode(id)} and result is: $result")
    result
  }

  //Проверка, "связки" и "проверки"
  override def unlock(proofs: Seq[ByteString]): Boolean = {
    val result: Boolean =
      check == Sha256RipeMD160(proofs.head ++ messageHash ++ metadata ++ publicKey)
    logger.info(s"Going to validate output: ${this.asJson}." +
      s"\nCheck is ${encode(check)}." +
      s"\nBundle from next tx is ${encode(proofs.head)}" +
      s"\nUnlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
    result && txNum > 0 && checkSignature
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputMessage {
  val typeId: Byte = 2: Byte
}
