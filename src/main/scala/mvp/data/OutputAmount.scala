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
    Decoder.forProduct3[String, Long, String, OutputAmount]("publicKey", "amount", "signature"){
    case (publicKey, amount, signature) =>
      OutputAmount(
        decode(publicKey).getOrElse(Array.emptyByteArray),
        amount,
        decode(signature).getOrElse(Array.emptyByteArray)
      )
  }

  implicit val encodeOutputAmount: Encoder[OutputAmount] = Encoder.forProduct5("id", "type" ,"publicKey", "amount", "signature") { o =>
    (encode(o.id), typeId, encode(o.publicKey), o.amount, encode(o.signature))
  }
}
