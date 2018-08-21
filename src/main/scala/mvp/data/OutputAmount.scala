package mvp.data

import com.google.common.primitives.Longs
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16.{encode, decode}

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

  implicit val decodeOutputAmount: Decoder[OutputAmount] =
    Decoder.forProduct4[String, Long, String, Boolean, OutputAmount]("publicKey", "amount", "signature", "signature"){
    case (publicKey, amount, signature, canBeSpent) =>
      OutputAmount(
        decode(publicKey).getOrElse(Array.emptyByteArray),
        amount,
        decode(signature).getOrElse(Array.emptyByteArray),
        canBeSpent
      )
  }

  implicit val encodeOutputAmount: Encoder[OutputAmount] = Encoder.forProduct4("publicKey", "amount", "signature", "signature") { o =>
    (encode(o.publicKey), o.amount, encode(o.signature), o.canBeSpent)
  }
}
