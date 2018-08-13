package mvp.modifiers.blockchain

import com.google.common.primitives.{Bytes, Ints, Longs}
import io.circe.{Decoder, Encoder, HCursor}
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

  implicit val jsonDecoder: Decoder[Header] = (c: HCursor) => for {
    timestamp <- c.downField("timestamp").as[Long]
    height <- c.downField("height").as[Int]
    previousBlockHash <- c.downField("previousBlockHash").as[String]
    minerSignature <- c.downField("minerSignature").as[String]
    merkleTreeRoot <- c.downField("merkleTreeRoot").as[String]
  } yield Header(
    timestamp,
    height,
    Base16.decode(previousBlockHash).getOrElse(Array.emptyByteArray),
    Base16.decode(minerSignature).getOrElse(Array.emptyByteArray),
    Base16.decode(merkleTreeRoot).getOrElse(Array.emptyByteArray)
  )

  implicit val jsonEncoder: Encoder[Header] = (b: Header) => Map(
    "id" -> Base16.encode(b.id).asJson,
    "timestamp" -> b.timestamp.asJson,
    "height" -> b.height.asJson,
    "previousBlockHash" -> Base16.encode(b.previousBlockHash).asJson,
    "minerSignature" -> Base16.encode(b.minerSignature).asJson,
    "merkleTreeRoot" -> Base16.encode(b.merkleTreeRoot).asJson
  ).asJson
}
