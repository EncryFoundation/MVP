package mvp.stats

import akka.actor.Actor
import org.influxdb.{InfluxDB, InfluxDBFactory}
import mvp.MVP.settings
import mvp.stats.InfluxActor.CurrentBlockHeight

class InfluxActor extends Actor {

  val influxDB: InfluxDB = InfluxDBFactory.connect(settings.influxDB.url, settings.influxDB.login, settings.influxDB.password)

  influxDB.setRetentionPolicy("autogen")

  override def preStart(): Unit =
    influxDB.write(8089, s"""nodesStartTime value="${settings.mvpStat.nodeName}"""")

  override def receive: Receive = {
    case CurrentBlockHeight() =>
  }
}

object InfluxActor {

  case class CurrentBlockHeight()
}
