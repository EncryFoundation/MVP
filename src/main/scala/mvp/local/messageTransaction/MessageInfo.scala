package mvp.local.messageTransaction

import com.google.common.primitives.Bytes

case class MessageInfo(message: Array[Byte],
                       metaData: Array[Byte],
                       publicKey: Array[Byte]) {

  val messageToSign: Array[Byte] = Bytes.concat(message ++ metaData ++ publicKey)
}
