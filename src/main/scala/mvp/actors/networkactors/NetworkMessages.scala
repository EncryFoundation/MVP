package mvp.actors.networkactors

import akka.util.ByteString
import mvp.actors.Messages.MessageToSend

object NetworkMessages {
  val RequestLastInfo: MessageToSend = MessageToSend(ByteString.fromArray(Array.fill(32)(0)))
}

