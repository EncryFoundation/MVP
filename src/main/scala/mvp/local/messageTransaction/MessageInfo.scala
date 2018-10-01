package mvp.local.messageTransaction

import java.security.PublicKey
import akka.util.ByteString

case class MessageInfo(message: ByteString,
                       metaData: ByteString,
                       publicKey: ByteString) {

  val messageToSign: ByteString = message ++ metaData ++ publicKey
}
