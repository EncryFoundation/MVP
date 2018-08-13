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
    bundle <- c.downField("bundle").as[String]
    check <- c.downField("check").as[String]
    publicKeyHash <- c.downField("publicKeyHash").as[String]
    userData <- c.downField("userData").as[String]
    publicKey <- c.downField("publicKey").as[String]
    signature <- c.downField("signature").as[String]
  } yield PKIOutput(
    Base58.decode(bundle).get,
    Base58.decode(check).get,
    Base58.decode(publicKeyHash).get,
    Base58.decode(userData).get,
    Base58.decode(publicKey).get,
    Base58.decode(signature).get
  )

  implicit val jsonEncoder: Encoder[PKIOutput] = (b: PKIOutput) => Map(
    "id" -> Base58.encode(b.id).asJson,
    "type" -> typeId.asJson,
    "bundle" -> Base16.encode(b.bundle).asJson,
    "check" -> Base16.encode(b.check).asJson,
    "publicKeyHash" -> Base16.encode(b.publicKeyHash).asJson,
    "userData" -> Base16.encode(b.userData).asJson,
    "publicKey" -> Base16.encode(b.publicKey).asJson,
    "signature" -> Base16.encode(b.signature).asJson,
  ).asJson
}
