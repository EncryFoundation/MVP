package mvp.data

import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, DecodingFailure, Encoder}
import scorex.crypto.signatures.{Curve25519, PublicKey, Signature}
import mvp.utils.BlockchainUtils._

trait Output extends Modifier with StrictLogging {

  val messageToSign: ByteString

  val publicKey: ByteString

  val signature: ByteString

  val canBeSpent: Boolean

  def checkSignature: Boolean = {
    val result: Boolean = Curve25519.verify(Signature @@ signature.toArray, messageToSign.toArray, PublicKey @@ publicKey.toArray)
    logger.info(s"Going to check signature for output with id: ${base16Encode(id)} and result is: $result")
    result
  }

  def closeForSpent: Output

  def unlock(proofs: Seq[ByteString]): Boolean
}

object Output {

  implicit val jsonDencoder: Decoder[Output] = {
    Decoder.instance { ins =>
      ins.downField("type").as[Byte] match {
        case Right(outputTypeId) => outputTypeId match {
          case OutputAmount.typeId => OutputAmount.decodeOutputAmount(ins)
          case OutputPKI.typeId => OutputPKI.decodeOutputPKI(ins)
          case OutputMessage.typeId => OutputMessage.decodeOutputMessage(ins)
        }
        case Left(_) => Left(DecodingFailure("None typeId", ins.history))
      }

    }
  }

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: OutputAmount => OutputAmount.encodeOutputAmount(ab)
    case db: OutputPKI => OutputPKI.encodeOutputPKI(db)
    case aib: OutputMessage => OutputMessage.encodeOutputMessage(aib)
  }
}