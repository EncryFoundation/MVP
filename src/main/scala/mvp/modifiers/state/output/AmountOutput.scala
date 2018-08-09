package mvp.modifiers.state.output

import com.google.common.primitives.Longs
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.syntax._
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58

case class AmountOutput(publicKey: Array[Byte],
                        amount: Long) extends Output {

  override def unlock(proof: Array[Byte]): Boolean = true

  override val id: Array[Byte] = Sha256RipeMD160(publicKey ++ Longs.toByteArray(amount))
}

object AmountOutput {

  val typeId: Byte = 0: Byte

  implicit val jsonDecoder: Decoder[AmountOutput] = (c: HCursor) => for {
    publicKey <- c.downField("publicKey").as[Array[Byte]]
    amount <- c.downField("amount").as[Long]
  } yield AmountOutput(
    publicKey,
    amount
  )

  implicit val jsonEncoder: Encoder[AmountOutput] = (b: AmountOutput) => Map(
    "type" -> typeId.asJson,
    "publicKey" -> Base58.encode(b.publicKey).asJson,
    "amount" -> b.amount.asJson
  ).asJson
}
