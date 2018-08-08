package mvp.stats

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import org.influxdb.{InfluxDB, InfluxDBFactory}
import mvp.MVP.settings
import mvp.stats.InfluxActor.CurrentBlockHeight

class InfluxActor extends Actor with StrictLogging {

  val influxDB: InfluxDB =
    InfluxDBFactory.connect(settings.influxDB.url, settings.influxDB.login, settings.influxDB.password)

  influxDB.setRetentionPolicy("autogen")

  override def preStart(): Unit =
    influxDB.write(8089, s"""nodesStartTime value="${settings.mvpStat.nodeName}"""")

  override def postStop(): Unit = {
    influxDB.close()
    logger.info(s"InfluxDB actor stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    logger.info(s"Restarted with reason $reason")
  }

  override def receive: Receive = {
    case CurrentBlockHeight() => println(influxDB.ping())
  }
}

object InfluxActor {

  case class CurrentBlockHeight()
}
