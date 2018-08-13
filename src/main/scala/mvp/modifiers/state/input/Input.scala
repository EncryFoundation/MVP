package mvp.modifiers.state.input

import io.circe.{Decoder, Encoder, HCursor}
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16
import io.circe.syntax._

case class Input(useOutputId: Array[Byte],
                 proof: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proof)
}

object Input {

  implicit val jsonDecoder: Decoder[Input] = (c: HCursor) => for {
    useOutputId <- c.downField("useOutputId").as[String]
    proof <- c.downField("proof").as[String]
  } yield Input(
    Base58.decode(useOutputId).get,
    Base58.decode(proof).get
  )

  implicit val jsonEncoder: Encoder[Input] = (b: Input) => Map(
    "useOutputId" -> Base16.encode(b.useOutputId).asJson,
    "proof" -> Base16.encode(b.proof).asJson
  ).asJson
}
