package mvp.modifiers.blockchain

import com.google.common.primitives.{Bytes, Ints, Longs}
import io.circe.Encoder
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160
import io.circe.syntax._
import scorex.crypto.encode.Base16

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

  implicit val jsonEncoder: Encoder[Header] = (b: Header) => Map(
    "timestamp" -> b.timestamp.asJson,
    "height" -> b.height.asJson,
    "previousBlockHash" -> Base16.encode(b.previousBlockHash).asJson,
    "minerSignature" -> Base16.encode(b.minerSignature).asJson,
    "merkleTreeRoot" -> Base16.encode(b.merkleTreeRoot).asJson
  ).asJson
}
