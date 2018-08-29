package mvp.utils

import akka.util.ByteString
import mvp.utils.BlockchainUtils.{base16Decode, base16Encode}
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

object EncodingUtils {

  implicit val byteStringEncoder: Encoder[ByteString] = bytes => base16Encode(bytes).asJson
  implicit val byteStringDecoder: Decoder[ByteString] =
    Decoder.decodeString.map(base16Decode).map(_.getOrElse(ByteString.empty))

}
