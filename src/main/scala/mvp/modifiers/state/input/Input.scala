package mvp.modifiers.state.input

import io.circe.{Decoder, Encoder, HCursor}
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base58
import io.circe.syntax._

case class Input(useOutputId: Array[Byte],
                 proof: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proof)
}

object Input {

  implicit val jsonDecoder: Decoder[Input] = (c: HCursor) => for {
    useOutputId <- c.downField("useOutputId").as[Array[Byte]]
    proof <- c.downField("proof").as[Array[Byte]]
  } yield Input(
    useOutputId,
    proof
  )

  implicit val jsonEncoder: Encoder[Input] = (b: Input) => Map(
    "useOutputId" -> Base58.encode(b.useOutputId).asJson,
    "proof" -> Base58.encode(b.proof).asJson
  ).asJson
}
