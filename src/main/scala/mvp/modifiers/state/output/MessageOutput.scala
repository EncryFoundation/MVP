package mvp.modifiers.state.output

import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16
import io.circe.syntax._
import mvp.local.messageTransaction.MessageInfo

case class MessageOutput(bundle: Array[Byte],
                         check: Array[Byte],
                         messageHash: Array[Byte],
                         metadata: Array[Byte],
                         publicKey: Array[Byte],
                         signature: Array[Byte],
                         override val canBeSpent: Boolean = true) extends Output with StrictLogging {

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
      logger.info(s"Going to validate output: ${MessageOutput.jsonEncoder(this)}." +
        s"Check is ${Base16.encode(check)}." +
        s"Bundle from next tx is ${Base16.encode(proofs.last)}" +
        s"Unlock condition \'check = Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)\' is $result")
      result
  }

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object MessageOutput {

  val typeId: Byte = 2: Byte

  implicit val jsonDecoder: Decoder[MessageOutput] = (c: HCursor) => for {
    bundle <- c.downField("bundle").as[String]
    check <- c.downField("check").as[String]
    messageHash <- c.downField("messageHash").as[String]
    metadata <- c.downField("metadata").as[String]
    publicKey <- c.downField("publicKey").as[String]
    signature <- c.downField("signature").as[String]
  } yield MessageOutput(
    Base16.decode(bundle).getOrElse(Array.emptyByteArray),
    Base16.decode(check).getOrElse(Array.emptyByteArray),
    Base16.decode(messageHash).getOrElse(Array.emptyByteArray),
    Base16.decode(metadata).getOrElse(Array.emptyByteArray),
    Base16.decode(publicKey).getOrElse(Array.emptyByteArray),
    Base16.decode(signature).getOrElse(Array.emptyByteArray)
  )

  implicit val jsonEncoder: Encoder[MessageOutput] = (b: MessageOutput) => Map(
    "id" -> Base16.encode(b.id).asJson,
    "type" -> typeId.asJson,
    "bundle" -> Base16.encode(b.bundle).asJson,
    "check" -> Base16.encode(b.check).asJson,
    "messageHash" -> Base16.encode(b.messageHash).asJson,
    "metadata" -> Base16.encode(b.metadata).asJson,
    "publicKey" -> Base16.encode(b.publicKey).asJson,
    "signature" -> Base16.encode(b.signature).asJson,
  ).asJson
}
