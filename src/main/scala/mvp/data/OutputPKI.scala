package mvp.data

import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16.{encode, decode}

case class OutputPKI(bundle: Array[Byte],
                     check: Array[Byte],
                     publicKeyHash: Array[Byte],
                     userData: Array[Byte],
                     publicKey: Array[Byte],
                     signature: Array[Byte],
                     override val canBeSpent: Boolean = true) extends Output {

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey ++ signature
  )

  override val messageToSign: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey
  )

  override def unlock(proofs: Seq[Array[Byte]]): Boolean = ???

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputPKI {

  val typeId: Byte = 1: Byte

  implicit val decodeOutputPKI: Decoder[OutputPKI] =
    Decoder.forProduct6[String, String, String, String, String, String, OutputPKI]("bundle", "check", "publicKeyHash", "userData", "publicKey", "signature") {
      case (bundle, check, publicKeyHash, userData, publicKey, signature) =>
        OutputPKI(
          decode(bundle).getOrElse(Array.emptyByteArray),
          decode(check).getOrElse(Array.emptyByteArray),
          decode(publicKeyHash).getOrElse(Array.emptyByteArray),
          decode(userData).getOrElse(Array.emptyByteArray),
          decode(publicKey).getOrElse(Array.emptyByteArray),
          decode(signature).getOrElse(Array.emptyByteArray)
        )
    }

  implicit val encodeOutputPKI: Encoder[OutputPKI] =
    Encoder.forProduct8("id", "type" ,"bundle", "check", "publicKeyHash", "userData", "publicKey", "signature") { o =>
      (encode(o.id), typeId, encode(o.bundle), encode(o.check), encode(o.publicKeyHash), encode(o.userData), encode(o.publicKeyHash), encode(o.signature))
    }

}
