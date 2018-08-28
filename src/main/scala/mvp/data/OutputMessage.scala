package mvp.data

import io.circe.{Decoder, Encoder}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16.{encode, decode}

case class OutputMessage(bundle: Array[Byte],
                         check: Array[Byte],
                         messageHash: Array[Byte],
                         metadata: Array[Byte],
                         publicKey: Array[Byte],
                         signature: Array[Byte],
                         txNum: Int,
                         override val canBeSpent: Boolean = true) extends Output {

  def toProofGenerator: MessageInfo = MessageInfo(messageHash, metadata, publicKey)

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey ++ signature
  )

  override val messageToSign: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey
  )

  //Проверка, "связки" и "проверки"
  override def unlock(proofs: Seq[Array[Byte]]): Boolean = {
    val result: Boolean = check sameElements Sha256RipeMD160(proofs.last ++ messageHash ++ metadata ++ publicKey)
    logger.info(s"Going to validate output: ${OutputMessage.encodeOutputMessage(this)}." +
      s"\nCheck is ${encode(check)}." +
      s"\nBundle from next tx is ${encode(proofs.last)}" +
      s"\nUnlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
    result && txNum > 0
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputMessage {

  val typeId: Byte = 2: Byte

  implicit val encodeOutputMessage: Encoder[OutputMessage] =
    Encoder.forProduct9("id", "type", "bundle", "check", "messageHash", "metadata", "publicKey", "signature", "txNum") { o =>
      (encode(o.id), typeId, encode(o.bundle), encode(o.check), encode(o.messageHash), encode(o.metadata), encode(o.publicKey), encode(o.signature), o.txNum)
    }

  implicit val decodeOutputMessage: Decoder[OutputMessage] =
    Decoder.forProduct7[String, String, String, String, String, String, Int, OutputMessage]("bundle", "check", "messageHash", "metadata", "publicKey", "signature", "txNum") {
      case (bundle, check, messageHash, metadata, publicKey, signature, txNum) =>
        OutputMessage(
          decode(bundle).getOrElse(Array.emptyByteArray),
          decode(check).getOrElse(Array.emptyByteArray),
          decode(messageHash).getOrElse(Array.emptyByteArray),
          decode(metadata).getOrElse(Array.emptyByteArray),
          decode(publicKey).getOrElse(Array.emptyByteArray),
          decode(signature).getOrElse(Array.emptyByteArray),
          txNum
        )
    }
}
