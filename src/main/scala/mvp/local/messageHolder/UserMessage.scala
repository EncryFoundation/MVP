package mvp.local.messageHolder

import java.security.PublicKey
import akka.util.ByteString
import mvp.local.messageTransaction.MessageInfo
import mvp.crypto.Sha256.Sha256RipeMD160

case class UserMessage(message: String,
                       metadata: ByteString,
                       sender: PublicKey,
                       fee: Long,
                       msgNum: Int) {

  def toMsgInfo: MessageInfo = MessageInfo(
    Sha256RipeMD160(ByteString(message)),
    metadata,
    sender
  )
}