package mvp.modifiers.state.output

import com.google.common.primitives.Longs
import io.circe.Encoder
import io.circe.syntax._
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58

case class AmountOutput(publicKey: Array[Byte],
                        amount: Long) extends Output {

  override def unlock(proof: Array[Byte]): Boolean = true

  override val id: Array[Byte] = Sha256RipeMD160(publicKey ++ Longs.toByteArray(amount))
}

object AmountOutput {

  implicit val jsonEncoder: Encoder[AmountOutput] = (b: AmountOutput) => Map(
    "publicKey" -> Base58.encode(b.publicKey).asJson,
    "amount" -> b.amount.asJson
  ).asJson
}
