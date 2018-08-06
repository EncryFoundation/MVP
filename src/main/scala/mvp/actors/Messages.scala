package mvp.actors

object Messages {

  sealed trait Message

  final case object Start extends Message

  final case object Heartbeat extends Message

}
