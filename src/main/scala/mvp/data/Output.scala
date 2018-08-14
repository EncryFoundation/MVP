package mvp.data

import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, DecodingFailure, Encoder}
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
          case OutputAmount.typeId => OutputAmount.jsonDecoder(ins)
          case OutputPKI.typeId => OutputPKI.jsonDecoder(ins)
          case OutputMessage.typeId => OutputMessage.jsonDecoder(ins)
        }
        case Left(_) => Left(DecodingFailure("None typeId", ins.history))
      }

    }
  }

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: OutputAmount => OutputAmount.jsonEncoder(ab)
    case db: OutputPKI => OutputPKI.jsonEncoder(db)
    case aib: OutputMessage => OutputMessage.jsonEncoder(aib)
  }
}