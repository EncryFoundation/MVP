package mvp.data

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.encode.Base16

case class Input(useOutputId: Array[Byte],
                 proofs: Seq[Array[Byte]]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proofs.flatten)
}

object Input {

  implicit val jsonDecoder: Decoder[Input] = (c: HCursor) => for {
    useOutputId <- c.downField("useOutputId").as[String]
    proofs <- c.downField("proofs").as[Seq[String]]
  } yield Input(
    Base16.decode(useOutputId).getOrElse(Array.emptyByteArray),
    proofs.map(proof => Base16.decode(proof).getOrElse(Array.emptyByteArray))
  )

  implicit val jsonEncoder: Encoder[Input] = (b: Input) => Map(
    "useOutputId" -> Base16.encode(b.useOutputId).asJson,
    "proofs" -> b.proofs.map(Base16.encode).asJson
  ).asJson
}
