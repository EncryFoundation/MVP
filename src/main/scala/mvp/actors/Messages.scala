package mvp.actors

import mvp.data.{Blockchain, Header, Payload, Transaction}
import mvp.local.messageHolder.UserMessage

object Messages {

  sealed trait Message

  final case object Start extends Message

  final case object Heartbeat extends Message

  case object GetLastInfo extends Message

  case object CurrentBlockHeight

  case class InfoMessage(msg: UserMessage) extends Message

  case object GetLastBlock extends Message

  case class Headers(headers: Seq[Header]) extends Message

  case class Payloads(payloads: Seq[Payload]) extends Message

  case class Transactions(transaction: Seq[Transaction]) extends Message

  case class BlockchainAnswer(blockchain: Blockchain) extends Message

  case class HeadersAnswer(blockchain: Blockchain) extends Message

}