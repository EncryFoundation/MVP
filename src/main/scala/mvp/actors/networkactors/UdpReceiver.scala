package mvp.actors.networkactors

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import io.circe.parser.decode
import io.circe.generic.auto._
import mvp.utils.EncodingUtils._
import mvp.actors.LastInfo
import mvp.actors.Messages.{GetLastInfo, LastInfoFromRemote, ReceivedMessage}
import mvp.actors.networkactors.NetworkMessages.RequestLastInfo

class UdpReceiver extends Actor with StrictLogging {

  override def receive: Receive = {
    case ReceivedMessage(data) if data == RequestLastInfo.data =>
      logger.info("Received last info request")
      context.parent ! GetLastInfo
    case ReceivedMessage(data) if decode[LastInfo](data.utf8String).isRight =>
      decode[LastInfo](data.utf8String).map(context.parent ! LastInfoFromRemote(_))
    case ReceivedMessage(data) => logger.info(s"Got data from remote ${data.toString()}")
  }

}
