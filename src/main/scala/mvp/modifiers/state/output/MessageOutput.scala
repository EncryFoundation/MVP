package mvp.modifiers.state.output

import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58
import io.circe.syntax._
import mvp.local.messageTransaction.MessageInfo

case class MessageOutput(bundle: Array[Byte],
                         check: Array[Byte],
                         messageHash: Array[Byte],
                         metadata: Array[Byte],
                         publicKey: Array[Byte],
                         signature: Array[Byte],
                         override val canBeSpent: Boolean = true) extends Output {

  def toMessageInfo(msg: String): MessageInfo = MessageInfo(msg.getBytes, metadata, publicKey)

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey ++ signature
  )

  override def unlock(proof: Array[Byte]): Boolean =
    check sameElements Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object MessageOutput {

  val typeId: Byte = 1: Byte

  implicit val jsonDecoder: Decoder[MessageOutput] = (c: HCursor) => for {
    bundle <- c.downField("bundle").as[Array[Byte]]
    check <- c.downField("check").as[Array[Byte]]
    messageHash <- c.downField("messageHash").as[Array[Byte]]
    metadata <- c.downField("metadata").as[Array[Byte]]
    publicKey <- c.downField("publicKey").as[Array[Byte]]
    signature <- c.downField("signature").as[Array[Byte]]
  } yield MessageOutput(
    bundle,
    check,
    messageHash,
    metadata,
    publicKey,
    signature
  )

  implicit val jsonEncoder: Encoder[MessageOutput] = (b: MessageOutput) => Map(
    "type" -> typeId.asJson,
    "bundle" -> Base58.encode(b.bundle).asJson,
    "check" -> Base58.encode(b.check).asJson,
    "messageHash" -> Base58.encode(b.messageHash).asJson,
    "metadata" -> Base58.encode(b.metadata).asJson,
    "publicKey" -> Base58.encode(b.publicKey).asJson,
    "signature" -> Base58.encode(b.signature).asJson,
  ).asJson
}
