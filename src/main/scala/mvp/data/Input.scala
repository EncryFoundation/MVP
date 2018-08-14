package mvp.data

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16

case class Input(useOutputId: Array[Byte],
                 proof: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proof)
}

object Input {

  implicit val jsonDecoder: Decoder[Input] = (c: HCursor) => for {
    useOutputId <- c.downField("useOutputId").as[String]
    proof <- c.downField("proof").as[String]
  } yield Input(
    Base16.decode(useOutputId).getOrElse(Array.emptyByteArray),
    Base16.decode(proof).getOrElse(Array.emptyByteArray)
  )

  implicit val jsonEncoder: Encoder[Input] = (b: Input) => Map(
    "useOutputId" -> Base16.encode(b.useOutputId).asJson,
    "proof" -> Base16.encode(b.proof).asJson
  ).asJson
}
