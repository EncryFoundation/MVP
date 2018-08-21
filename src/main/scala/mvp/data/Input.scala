package mvp.data

import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16.{encode, decode}

case class Input(useOutputId: Array[Byte],
                 proofs: Seq[Array[Byte]]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proofs.flatten)
}

object Input {

  implicit val decodeInput: Decoder[Input] =
    Decoder.forProduct2[String, Seq[String], Input]("useOutputId", "proofs") {
      case (useOutputId, proofs) =>
        Input(
          decode(useOutputId).getOrElse(Array.emptyByteArray),
          proofs.map(decode(_).getOrElse(Array.emptyByteArray))
        )
    }

  implicit val encodeInput: Encoder[Input] =
    Encoder.forProduct2("useOutputId", "proofs") { i =>
      (encode(i.useOutputId), i.proofs.map(encode))
    }

}
