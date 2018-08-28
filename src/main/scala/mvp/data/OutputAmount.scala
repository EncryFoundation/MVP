package mvp.data

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class OutputAmount(publicKey: ByteString,
                        amount: Long,
                        signature: ByteString,
                        override val canBeSpent: Boolean = true) extends Output {

  override def closeForSpent: Output = this.copy(canBeSpent = false)

  override val messageToSign: ByteString = Sha256RipeMD160(publicKey ++ toByteArray(amount))

  override def unlock(proofs: Seq[ByteString]): Boolean = true

  override val id: ByteString = Sha256RipeMD160(publicKey ++ toByteArray(amount))
}

object OutputAmount {

  val typeId: Byte = 0: Byte

  implicit val decodeOutputAmount: Decoder[OutputAmount] =
    Decoder.forProduct3[String, Long, String, OutputAmount]("publicKey", "amount", "signature"){
    case (publicKey, amount, signature) =>
      OutputAmount(
        base16Decode(publicKey).getOrElse(ByteString.empty),
        amount,
        base16Decode(signature).getOrElse(ByteString.empty)
      )
  }

  implicit val encodeOutputAmount: Encoder[OutputAmount] = Encoder.forProduct5("id", "type" ,"publicKey", "amount", "signature") { o =>
    (base16Encode(o.id), typeId, base16Encode(o.publicKey), o.amount, base16Encode(o.signature))
  }
}
