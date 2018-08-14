package mvp.modifiers.state.output

import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, DecodingFailure, Encoder}
import mvp.modifiers.Modifier
import scorex.crypto.signatures.{Curve25519, PublicKey, Signature}
import scorex.util.encode.Base16

trait Output extends Modifier with StrictLogging {

  val messageToSign: Array[Byte]

  val publicKey: Array[Byte]

  val signature: Array[Byte]

  val canBeSpent: Boolean

  def checkSignature: Boolean = {
    val result: Boolean = Curve25519.verify(Signature @@ signature, messageToSign, PublicKey @@ publicKey)
    logger.info(s"Going to check signature for output with id: ${Base16.encode(id)} and result is: $result")
    result
  }

  def closeForSpent: Output

  def unlock(proofs: Seq[Array[Byte]]): Boolean
}

object Output {

  implicit val jsonDencoder: Decoder[Output] = {
    Decoder.instance { ins =>
      ins.downField("type").as[Byte] match {
        case Right(outputTypeId) => outputTypeId match {
          case AmountOutput.typeId => AmountOutput.jsonDecoder(ins)
          case PKIOutput.typeId => PKIOutput.jsonDecoder(ins)
          case MessageOutput.typeId => MessageOutput.jsonDecoder(ins)
        }
        case Left(_) => Left(DecodingFailure("None typeId", ins.history))
      }

    }
  }

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: AmountOutput => AmountOutput.jsonEncoder(ab)
    case db: PKIOutput => PKIOutput.jsonEncoder(db)
    case aib: MessageOutput => MessageOutput.jsonEncoder(aib)
  }
}