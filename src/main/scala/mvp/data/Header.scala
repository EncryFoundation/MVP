package mvp.data

import com.google.common.primitives.{Bytes, Ints, Longs}
import io.circe.{Decoder, Encoder}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16.{encode, decode}

case class Header(timestamp: Long,
                  height: Int,
                  previousBlockHash: Array[Byte],
                  minerSignature: Array[Byte],
                  merkleTreeRoot: Array[Byte]) extends Modifier {

  val messageToSign: Array[Byte] = Bytes.concat(
    Longs.toByteArray(timestamp) ++ Ints.toByteArray(height) ++ previousBlockHash ++ merkleTreeRoot
  )

  override val id: Array[Byte] = Sha256RipeMD160(
    Longs.toByteArray(timestamp) ++ Ints.toByteArray(height) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot
  )
}

object Header {

  implicit val decodeHeader: Decoder[Header] =
    Decoder.forProduct5[Long, Int, String, String, String, Header]("timestamp", "height", "previousBlockHash", "minerSignature", "merkleTreeRoot") {
      case (ts, height, previousBlockHash, minerSignature, merkleTreeRoot) =>
        Header(
          ts,
          height,
          decode(previousBlockHash).getOrElse(Array.emptyByteArray),
          decode(minerSignature).getOrElse(Array.emptyByteArray),
          decode(merkleTreeRoot).getOrElse(Array.emptyByteArray)
        )
    }

  implicit val encodeHeader: Encoder[Header] =
    Encoder.forProduct5("timestamp", "height", "previousBlockHash", "minerSignature", "merkleTreeRoot") { h =>
      (h.timestamp, h.height, encode(h.previousBlockHash), encode(h.minerSignature), encode(h.merkleTreeRoot))
    }

}
