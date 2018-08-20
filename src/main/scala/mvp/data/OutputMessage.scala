package mvp.data

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16

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
    logger.info(s"Going to validate output: ${OutputMessage.jsonEncoder(this)}." +
      s"\nCheck is ${Base16.encode(check)}." +
      s"\nBundle from next tx is ${Base16.encode(proofs.last)}" +
      s"\nUnlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
    result && txNum > 0
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputMessage {

  val typeId: Byte = 2: Byte

  implicit val jsonDecoder: Decoder[OutputMessage] = (c: HCursor) => for {
    bundle <- c.downField("bundle").as[String]
    check <- c.downField("check").as[String]
    messageHash <- c.downField("messageHash").as[String]
    metadata <- c.downField("metadata").as[String]
    publicKey <- c.downField("publicKey").as[String]
    signature <- c.downField("signature").as[String]
    txNum <- c.downField("txNum").as[Int]
  } yield OutputMessage(
    Base16.decode(bundle).getOrElse(Array.emptyByteArray),
    Base16.decode(check).getOrElse(Array.emptyByteArray),
    Base16.decode(messageHash).getOrElse(Array.emptyByteArray),
    Base16.decode(metadata).getOrElse(Array.emptyByteArray),
    Base16.decode(publicKey).getOrElse(Array.emptyByteArray),
    Base16.decode(signature).getOrElse(Array.emptyByteArray),
    txNum
  )

  implicit val jsonEncoder: Encoder[OutputMessage] = (b: OutputMessage) => Map(
    "id" -> Base16.encode(b.id).asJson,
    "type" -> typeId.asJson,
    "bundle" -> Base16.encode(b.bundle).asJson,
    "check" -> Base16.encode(b.check).asJson,
    "messageHash" -> Base16.encode(b.messageHash).asJson,
    "metadata" -> Base16.encode(b.metadata).asJson,
    "publicKey" -> Base16.encode(b.publicKey).asJson,
    "signature" -> Base16.encode(b.signature).asJson,
    "txNum" -> b.txNum.asJson,
  ).asJson
}
