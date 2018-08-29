package mvp.data

import akka.util.ByteString
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._
import mvp.utils.EncodingUtils._
import io.circe.syntax._
import io.circe.generic.auto._

case class OutputMessage(bundle: ByteString,
                         check: ByteString,
                         messageHash: ByteString,
                         metadata: ByteString,
                         publicKey: ByteString,
                         signature: ByteString,
                         txNum: Int,
                         override val canBeSpent: Boolean = true) extends Output {

  def toProofGenerator: MessageInfo = MessageInfo(messageHash, metadata, publicKey)

  override val id: ByteString = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey ++ signature
  )

  override val messageToSign: ByteString = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey
  )

  //Проверка, "связки" и "проверки"
  override def unlock(proofs: Seq[ByteString]): Boolean = {
    val result: Boolean = check == Sha256RipeMD160(proofs.last ++ messageHash ++ metadata ++ publicKey)
    logger.info(s"Going to validate output: ${this.asJson}." +
      s"\nCheck is ${base16Encode(check)}." +
      s"\nBundle from next tx is ${base16Encode(proofs.last)}" +
      s"\nUnlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
    result && txNum > 0
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputMessage {
  val typeId: Byte = 2: Byte
}
