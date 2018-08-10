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
                         signature: Array[Byte]) extends Output {

  def toProofGenerator: MessageInfo = MessageInfo(messageHash, metadata, publicKey)

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey ++ signature
  )

  override def unlock(proof: Array[Byte]): Boolean =
    check sameElements Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)
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
    Base58.decode(bundle).get,
    Base58.decode(check).get,
    Base58.decode(messageHash).get,
    Base58.decode(metadata).get,
    Base58.decode(publicKey).get,
    Base58.decode(signature).get
  )

  implicit val jsonEncoder: Encoder[MessageOutput] = (b: MessageOutput) => Map(
    "id" -> Base58.encode(b.id).asJson,
    "type" -> typeId.asJson,
    "bundle" -> Base58.encode(b.bundle).asJson,
    "check" -> Base58.encode(b.check).asJson,
    "messageHash" -> Base58.encode(b.messageHash).asJson,
    "metadata" -> Base58.encode(b.metadata).asJson,
    "publicKey" -> Base58.encode(b.publicKey).asJson,
    "signature" -> Base58.encode(b.signature).asJson,
  ).asJson
}
