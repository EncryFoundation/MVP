package mvp.modifiers.state.input

import io.circe.Encoder
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58
import io.circe.syntax._

case class Input(useOutputId: Array[Byte],
                 proof: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proof)
}

object Input {

  implicit val jsonEncoder: Encoder[Input] = (b: Input) => Map(
    "useOutputId" -> Base58.encode(b.useOutputId).asJson,
    "proof" -> Base58.encode(b.proof).asJson
  ).asJson
}
