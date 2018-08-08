package mvp.modifiers.state.output

import io.circe.{Decoder, Encoder}
import mvp.modifiers.Modifier

trait Output extends Modifier {

  def unlock(proof: Array[Byte]): Boolean
}

object Output {

  implicit val jsonDencoder: Decoder[Output] = {
    case ab: AmountOutput => AmountOutput.jsonDecoder(ab)
    case db: PKIOutput => PKIOutput.jsonDecoder(db)
    case aib: MessageOutput => MessageOutput.jsonDecoder(aib)
  }

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: AmountOutput => AmountOutput.jsonEncoder(ab)
    case db: PKIOutput => PKIOutput.jsonEncoder(db)
    case aib: MessageOutput => MessageOutput.jsonEncoder(aib)
  }
}