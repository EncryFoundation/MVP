package mvp.utils

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class Settings(thisNode: Node, otherNodes: List[Node], testMode: Boolean, heartbeat: Int)
case class Node(host: String, port: Int)

object Settings {
  def load: Settings = ConfigFactory.load("local.conf")
    .withFallback(ConfigFactory.load).as[Settings]
}


