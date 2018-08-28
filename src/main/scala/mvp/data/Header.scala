package mvp.data

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._

case class Header(timestamp: Long,
                  height: Int,
                  previousBlockHash: ByteString,
                  minerSignature: ByteString,
                  merkleTreeRoot: ByteString) extends Modifier {

  val messageToSign: ByteString =
    toByteArray(timestamp) ++ toByteArray(height) ++ previousBlockHash ++ merkleTreeRoot


  override val id: ByteString = Sha256RipeMD160(
    toByteArray(timestamp) ++ toByteArray(height) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot
  )
}

object Header {

  implicit val decodeHeader: Decoder[Header] =
    Decoder.forProduct5[Long, Int, String, String, String, Header]("timestamp", "height", "previousBlockHash", "minerSignature", "merkleTreeRoot") {
      case (ts, height, previousBlockHash, minerSignature, merkleTreeRoot) =>
        Header(
          ts,
          height,
          base16Decode(previousBlockHash).getOrElse(ByteString.empty),
          base16Decode(minerSignature).getOrElse(ByteString.empty),
          base16Decode(merkleTreeRoot).getOrElse(ByteString.empty)
        )
    }

  implicit val encodeHeader: Encoder[Header] =
    Encoder.forProduct5("timestamp", "height", "previousBlockHash", "minerSignature", "merkleTreeRoot") { h =>
      (h.timestamp, h.height, base16Encode(h.previousBlockHash), base16Encode(h.minerSignature), base16Encode(h.merkleTreeRoot))
    }

}
