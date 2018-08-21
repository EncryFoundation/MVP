package mvp.local.messageHolder

import io.circe.{Decoder, Encoder}
import scorex.util.encode.Base16.{encode, decode}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160

case class UserMessage(message: String, metadata: Array[Byte], sender: Array[Byte], prevOutputId: Option[Array[Byte]], msgNub: Int) {

  def toMsgInfo: MessageInfo = MessageInfo(
    Sha256RipeMD160(message.getBytes),
    metadata,
    sender
  )
}

object UserMessage {

  implicit val decodeUserMessage: Decoder[UserMessage] =
    Decoder.forProduct4[String, String, String, Int, UserMessage]("message", "metadata", "sender", "msgNub") {
      case (message, metadata, sender, msgNub) =>
        UserMessage(
          message,
          decode(metadata).getOrElse(Array.emptyByteArray),
          decode(sender).getOrElse(Array.emptyByteArray),
          None,
          msgNub
        )
    }

  implicit val encodeUserMessage: Encoder[UserMessage] =
    Encoder.forProduct4("message", "metadata", "sender", "msgNub") { um =>
      (um.message, encode(um.metadata), encode(um.sender), um.msgNub)
    }
}