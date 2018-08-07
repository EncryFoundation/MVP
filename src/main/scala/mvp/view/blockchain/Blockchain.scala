package mvp.view.blockchain

import mvp.modifiers.blockchain.{Block, Header, Payload}

//TODO: Blocks to levelDb

case class Blockchain(headers: Seq[Header] = Seq.empty, blocks: Seq[Block] = Seq.empty) {

  val headersHeight: Int = if (headers.nonEmpty) headers.last.height else Blockchain.genesisHeight

  val blockchainHeight: Int = if (blocks.nonEmpty) blocks.last.header.height else Blockchain.genesisHeight

  def addPayload(payload: Payload): Blockchain = Blockchain(headers, headers.find(_.merkleTreeRoot sameElements payload.id).map(header => blocks :+ Block(header, payload)).getOrElse(blocks))

  def addHeader(headerToAdd: Header): Blockchain = Blockchain(headers :+ headerToAdd, blocks)

  def getHeaderAtHeight(height: Int): Option[Header] = headers.find(_.height == height)
}

object Blockchain {

  val genesisHeight: Int = -1

  val emptyBlockchain: Blockchain = Blockchain()

  //TODO: Recover from levelDb
  def recoverBlockchain: Blockchain = emptyBlockchain
}