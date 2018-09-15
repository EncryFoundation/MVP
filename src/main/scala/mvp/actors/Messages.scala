package mvp.actors

import akka.util.ByteString
import mvp.data.{Blockchain, Header, Payload, Transaction}
import mvp.local.messageHolder.UserMessage

object Messages {

  sealed trait Message

  final case object Start extends Message

  final case object Heartbeat extends Message

  final case object GetLastInfo extends Message

  final case object CurrentBlockHeight

  final case class InfoMessage(msg: UserMessage) extends Message

  final case object GetLastBlock extends Message

  final case class Headers(headers: Seq[Header]) extends Message

  final case class Payloads(payloads: Seq[Payload]) extends Message

  final case class Transactions(transaction: Seq[Transaction]) extends Message

  final case class BlockchainAnswer(blockchain: Blockchain) extends Message

  final case class HeadersAnswer(blockchain: Blockchain) extends Message

  final case class MessageToSend(data: ByteString) extends Message

  final case class ReceivedMessage(data: ByteString) extends Message

  final case class LastInfoFromRemote(lastInfo: LastInfo) extends Message

}