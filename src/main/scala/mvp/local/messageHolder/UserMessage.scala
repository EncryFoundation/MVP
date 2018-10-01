package mvp.local.messageHolder

import akka.util.ByteString
import mvp.local.messageTransaction.MessageInfo
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.Base16

case class UserMessage(message: String,
                       metadata: ByteString,
                       sender: ByteString,
                       fee: Long,
                       msgNum: Int) {

  def toMsgInfo: MessageInfo = MessageInfo(
    Sha256RipeMD160(Base16.decode(message).getOrElse(ByteString(message))),
    metadata,
    sender
  )
}