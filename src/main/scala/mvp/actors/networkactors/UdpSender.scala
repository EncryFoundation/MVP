package mvp.actors.networkactors

import akka.actor.Actor
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.LastInfo
import io.circe.syntax._
import io.circe.generic.auto._
import mvp.utils.EncodingUtils._

class UdpSender extends Actor with StrictLogging {

  override def receive: Receive = {
    case li: LastInfo => context.actorSelection("/user/starter/networker/udp") ! MessageToSend(ByteString(li.asJson.toString))
    case msg: MessageToSend => context.actorSelection("/user/starter/networker/udp") ! msg
  }

}