package mvp.data

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

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
    logger.info(s"Going to validate output: ${OutputMessage.encodeOutputMessage(this)}." +
      s"\nCheck is ${base16Encode(check)}." +
      s"\nBundle from next tx is ${base16Encode(proofs.last)}" +
      s"\nUnlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
    result && txNum > 0
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputMessage {

  val typeId: Byte = 2: Byte

  implicit val encodeOutputMessage: Encoder[OutputMessage] =
    Encoder.forProduct9("id", "type", "bundle", "check", "messageHash", "metadata", "publicKey", "signature", "txNum") { o =>
      (base16Encode(o.id), typeId, base16Encode(o.bundle), base16Encode(o.check), base16Encode(o.messageHash), base16Encode(o.metadata), base16Encode(o.publicKey), base16Encode(o.signature), o.txNum)
    }

  implicit val decodeOutputMessage: Decoder[OutputMessage] =
    Decoder.forProduct7[String, String, String, String, String, String, Int, OutputMessage]("bundle", "check", "messageHash", "metadata", "publicKey", "signature", "txNum") {
      case (bundle, check, messageHash, metadata, publicKey, signature, txNum) =>
        OutputMessage(
          base16Decode(bundle).getOrElse(ByteString.empty),
          base16Decode(check).getOrElse(ByteString.empty),
          base16Decode(messageHash).getOrElse(ByteString.empty),
          base16Decode(metadata).getOrElse(ByteString.empty),
          base16Decode(publicKey).getOrElse(ByteString.empty),
          base16Decode(signature).getOrElse(ByteString.empty),
          txNum
        )
    }
}
