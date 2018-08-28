package mvp.data

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class OutputPKI(bundle: ByteString,
                     check: ByteString,
                     publicKeyHash: ByteString,
                     userData: ByteString,
                     publicKey: ByteString,
                     signature: ByteString,
                     override val canBeSpent: Boolean = true) extends Output {

  override val id: ByteString = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey ++ signature
  )

  override val messageToSign: ByteString = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey
  )

  override def unlock(proofs: Seq[ByteString]): Boolean = ???

  override def closeForSpent: Output = this.copy(canBeSpent = false)
}

object OutputPKI {

  val typeId: Byte = 1: Byte

  implicit val decodeOutputPKI: Decoder[OutputPKI] =
    Decoder.forProduct6[String, String, String, String, String, String, OutputPKI]("bundle", "check", "publicKeyHash", "userData", "publicKey", "signature") {
      case (bundle, check, publicKeyHash, userData, publicKey, signature) =>
        OutputPKI(
          base16Decode(bundle).getOrElse(ByteString.empty),
          base16Decode(check).getOrElse(ByteString.empty),
          base16Decode(publicKeyHash).getOrElse(ByteString.empty),
          base16Decode(userData).getOrElse(ByteString.empty),
          base16Decode(publicKey).getOrElse(ByteString.empty),
          base16Decode(signature).getOrElse(ByteString.empty)
        )
    }

  implicit val encodeOutputPKI: Encoder[OutputPKI] =
    Encoder.forProduct8("id", "type" ,"bundle", "check", "publicKeyHash", "userData", "publicKey", "signature") { o =>
      (base16Encode(o.id), typeId, base16Encode(o.bundle), base16Encode(o.check), base16Encode(o.publicKeyHash), base16Encode(o.userData), base16Encode(o.publicKeyHash), base16Encode(o.signature))
    }

}
