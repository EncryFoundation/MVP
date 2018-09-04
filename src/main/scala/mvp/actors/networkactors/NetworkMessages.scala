package mvp.actors.networkactors

import akka.util.ByteString
import mvp.actors.LastInfo

object NetworkMessages {
  val RequestLastInfo: MessageToSend = MessageToSend(ByteString.fromArray(Array.fill(32)(0)))
}

case class MessageToSend(data: ByteString)
case class ReceivedMessage(data: ByteString)
case class LastInfoFromRemote(lastInfo: LastInfo)