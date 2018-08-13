package mvp.actors

import mvp.local.messageHolder.UserMessage
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.view.blockchain.Blockchain

object Messages {

  sealed trait Message

  final case object Start extends Message

  final case object Heartbeat extends Message

  case object GetLastInfo

  case class ThisMessage(msg: UserMessage)

  case object GetLastBlock

  case class Headers(headers: Seq[Header])

  case class Payloads(payloads: Seq[Payload])

  case class Transactions(transaction: Seq[Transaction])

  case class BlockchainAnswer(blockchain: Blockchain)

  case class HeadersAnswer(blockchain: Blockchain)

}
