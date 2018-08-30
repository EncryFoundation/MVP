package mvp.utils

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class Settings(thisNode: Node,
                    otherNodes: List[Node],
                    testMode: Boolean,
                    heartbeat: Int,
                    influxDB: InfluxDBSettings,
                    mvpSettings: mvpSettings,
                    levelDB: LevelDBSettings)

case class Node(host: String, port: Int)

case class InfluxDBSettings(url: String,
                            login: String,
                            password: String)


case class mvpSettings(enableCLI: Boolean,
                       nodeName: String,
                       sendStat: Boolean,
                       messagesQtyInChain: Int,
                       useUDP: Boolean)

case class LevelDBSettings(enable: Boolean,
                           recoverMode: Boolean)
object Settings {
  def load: Settings = ConfigFactory.load("local.conf")
    .withFallback(ConfigFactory.load).as[Settings]
}


