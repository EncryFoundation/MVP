package mvp.actors

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages.CurrentBlockHeight
import org.influxdb.{InfluxDB, InfluxDBFactory}

class InfluxActor extends Actor with StrictLogging {

  val influxDB: InfluxDB =
    InfluxDBFactory.connect(settings.influxDB.url, settings.influxDB.login, settings.influxDB.password)

  influxDB.setRetentionPolicy("autogen")

  override def preStart(): Unit =
    influxDB.write(8089, s"""nodestarttime value="${settings.mvpSettings.nodeName}"""")

  override def postStop(): Unit = {
    influxDB.close()
    logger.info(s"InfluxDB actor stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    logger.info(s"Restarted with reason $reason")
  }

  override def receive: Receive = {
    case CurrentBlockHeight =>
  }
}