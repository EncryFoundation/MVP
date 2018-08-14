package mvp.data

import io.circe.{Decoder, DecodingFailure, Encoder}

trait Output extends Modifier {

  val canBeSpent: Boolean

  def closeForSpent: Output

  def unlock(proof: Array[Byte]): Boolean
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