package mvp.modifiers.state.output

import io.circe.Encoder
import mvp.modifiers.Modifier

trait Output extends Modifier {

  def unlock(proof: Array[Byte]): Boolean
}

object Output {

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: AmountOutput => AmountOutput.jsonEncoder(ab)
    case db: PKIOutput => PKIOutput.jsonEncoder(db)
    case aib: MessageOutput => MessageOutput.jsonEncoder(aib)
  }
}