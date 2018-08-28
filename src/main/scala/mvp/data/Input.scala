package mvp.data

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class Input(useOutputId: ByteString,
                 proofs: Seq[ByteString]) extends Modifier {

  override val id: ByteString = Sha256RipeMD160(useOutputId ++ proofs.flatten)
}

object Input {

  implicit val decodeInput: Decoder[Input] =
    Decoder.forProduct2[String, Seq[String], Input]("useOutputId", "proofs") {
      case (useOutputId, proofs) =>
        Input(
          base16Decode(useOutputId).getOrElse(ByteString.empty),
          proofs.map(base16Decode(_).getOrElse(ByteString.empty))
        )
    }

  implicit val encodeInput: Encoder[Input] =
    Encoder.forProduct2("useOutputId", "proofs") { i =>
      (base16Encode(i.useOutputId), i.proofs.map(base16Encode))
    }

}
