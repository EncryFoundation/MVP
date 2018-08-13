package mvp.modifiers.state.output

import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16
import io.circe.syntax._

case class PKIOutput(bundle: Array[Byte],
                     check: Array[Byte],
                     publicKeyHash: Array[Byte],
                     userData: Array[Byte],
                     publicKey: Array[Byte],
                     signature: Array[Byte],
                     override val canBeSpent: Boolean = true) extends Output {

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey ++ signature
  )

  override def unlock(proof: Array[Byte]): Boolean = ???

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object PKIOutput {

  val typeId: Byte = 1: Byte

  implicit val jsonDecoder: Decoder[PKIOutput] = (c: HCursor) => for {
    bundle <- c.downField("bundle").as[Array[Byte]]
    check <- c.downField("check").as[Array[Byte]]
    publicKeyHash <- c.downField("publicKeyHash").as[Array[Byte]]
    userData <- c.downField("userData").as[Array[Byte]]
    publicKey <- c.downField("publicKey").as[Array[Byte]]
    signature <- c.downField("signature").as[Array[Byte]]
  } yield PKIOutput(
    bundle,
    check,
    publicKeyHash,
    userData,
    publicKey,
    signature
  )

  implicit val jsonEncoder: Encoder[PKIOutput] = (b: PKIOutput) => Map(
    "type" -> typeId.asJson,
    "bundle" -> Base16.encode(b.bundle).asJson,
    "check" -> Base16.encode(b.check).asJson,
    "publicKeyHash" -> Base16.encode(b.publicKeyHash).asJson,
    "userData" -> Base16.encode(b.userData).asJson,
    "publicKey" -> Base16.encode(b.publicKey).asJson,
    "signature" -> Base16.encode(b.signature).asJson,
  ).asJson
}
