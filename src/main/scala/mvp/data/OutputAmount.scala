package mvp.data

import com.google.common.primitives.Longs
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16

case class OutputAmount(publicKey: Array[Byte],
                        amount: Long,
                        signature: Array[Byte],
                        override val canBeSpent: Boolean = true) extends Output {

  override def closeForSpent: Output = this.copy(canBeSpent = false)

  override val messageToSign: Array[Byte] = Sha256RipeMD160(publicKey ++ Longs.toByteArray(amount))

  override def unlock(proofs: Seq[Array[Byte]]): Boolean = true

  override val id: Array[Byte] = Sha256RipeMD160(publicKey ++ Longs.toByteArray(amount))
}

object OutputAmount {

  val typeId: Byte = 0: Byte

  implicit val jsonDecoder: Decoder[OutputAmount] = (c: HCursor) => for {
    publicKey <- c.downField("publicKey").as[String]
    amount <- c.downField("amount").as[Long]
    signature <- c.downField("signature").as[String]
  } yield AmountOutput(
    Base16.decode(publicKey).getOrElse(Array.emptyByteArray),
    amount,
    Base16.decode(signature).getOrElse(Array.emptyByteArray)
  )

  implicit val jsonEncoder: Encoder[OutputAmount] = (b: OutputAmount) => Map(
    "id" -> Base16.encode(b.id).asJson,
    "type" -> typeId.asJson,
    "publicKey" -> Base16.encode(b.publicKey).asJson,
    "amount" -> b.amount.asJson,
    "signaturu" -> Base16.encode(b.signature).asJson,
  ).asJson
}
