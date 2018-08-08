package mvp.modifiers.state.output

import io.circe.Encoder
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58
import io.circe.syntax._

case class MessageOutput(bundle: Array[Byte],
                         check: Array[Byte],
                         messageHash: Array[Byte],
                         metadata: Array[Byte],
                         publicKey: Array[Byte],
                         signature: Array[Byte]) extends Output {

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ messageHash ++ metadata ++ publicKey ++ signature
  )

  override def unlock(proof: Array[Byte]): Boolean =
    check sameElements Sha256RipeMD160(proof ++ messageHash ++ metadata ++ publicKey)
}

object MessageOutput {

  implicit val jsonEncoder: Encoder[MessageOutput] = (b: MessageOutput) => Map(
    "bundle" -> Base58.encode(b.bundle).asJson,
    "check" -> Base58.encode(b.check).asJson,
    "messageHash" -> Base58.encode(b.messageHash).asJson,
    "metadata" -> Base58.encode(b.metadata).asJson,
    "publicKey" -> Base58.encode(b.publicKey).asJson,
    "signature" -> Base58.encode(b.signature).asJson,
  ).asJson
}
