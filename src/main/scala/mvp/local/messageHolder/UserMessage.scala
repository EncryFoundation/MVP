package mvp.local.messageHolder

import io.circe.{Decoder, Encoder}
import scorex.util.encode.Base16.{encode, decode}

case class UserMessage(message: String, sender: Array[Byte], prevOutputId: Option[Array[Byte]])

object UserMessage {

  implicit val decodeUserMessage: Decoder[UserMessage] =
    Decoder.forProduct3[String, String, String, UserMessage]("message", "sender", "prevOutputId") {
      case (message, sender, prevOutputId) =>
        UserMessage(
          message,
          decode(sender).getOrElse(Array.emptyByteArray),
          decode(prevOutputId).toOption
        )
    }

  implicit val encodeUserMessage: Encoder[UserMessage] =
    Encoder.forProduct3("message", "sender", "prevOutputId") { um =>
      (um.message, encode(um.sender), um.prevOutputId.map(encode))
    }
}