package mvp.local.messageHolder

import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.BlockchainUtils._
import mvp.utils.Crypto.Sha256RipeMD160

case class UserMessage(message: String, metadata: ByteString, sender: ByteString, prevOutputId: Option[ByteString], msgNum: Int) {

  def toMsgInfo: MessageInfo = MessageInfo(
    Sha256RipeMD160(ByteString(message)),
    metadata,
    sender
  )
}

object UserMessage {

  implicit val decodeUserMessage: Decoder[UserMessage] =
    Decoder.forProduct4[String, String, String, Int, UserMessage]("message", "metadata", "sender", "msgNum") {
      case (message, metadata, sender, msgNub) =>
        UserMessage(
          message,
          base16Decode(metadata).getOrElse(ByteString.empty),
          base16Decode(sender).getOrElse(ByteString.empty),
          None,
          msgNub
        )
    }

  implicit val encodeUserMessage: Encoder[UserMessage] =
    Encoder.forProduct4("message", "metadata", "sender", "msgNum") { um =>
      (um.message, base16Encode(um.metadata), base16Encode(um.sender), um.msgNum)
    }
}