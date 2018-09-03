package mvp.local.messageTransaction

import akka.util.ByteString

case class MessageInfo(message: ByteString,
                       metaData: ByteString,
                       publicKey: ByteString) {

  val messageToSign: ByteString = message ++ metaData ++ publicKey
}
