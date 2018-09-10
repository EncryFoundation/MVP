package mvp.data

import akka.util.ByteString
import mvp.MVP.system
import mvp.actors.ModifiersHolder.RequestModifiers

//TODO: Blocks to levelDb

case class Blockchain(headers: Seq[Header] = Seq.empty, blocks: Seq[Block] = Seq.empty) {

  val headersHeight: Int = headers.lastOption.map(_.height).getOrElse(Blockchain.genesisHeight)

  val blockchainHeight: Int = blocks.lastOption.map(_.header.height).getOrElse(Blockchain.genesisHeight)

  val lastBlock: Option[Block] = blocks.lastOption

  def addPayload(payload: Payload): Blockchain = Blockchain(
    headers,
    headers.find(_.merkleTreeRoot == payload.id)
      .map(header => {
        (blocks :+ Block(header, payload))
          .sortWith((blockOne, blockTwo) => blockOne.header.height < blockTwo.header.height)
      })
      .getOrElse(blocks)
  )

  def sendBlock: Unit = blocks.lastOption.foreach(block =>
    system.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(block))

  def addHeader(headerToAdd: Header): Blockchain = Blockchain(headers :+ headerToAdd, blocks)

  def getHeaderAtHeight(height: Int): Option[Header] = headers.find(_.height == height)
}

object Blockchain {

  val genesisHeight: Int = -1

  val genesisBlockchain: Blockchain = {
    val genesisBlock: Block =
      Block(Header(1L, 0, ByteString.empty, ByteString.empty, ByteString.empty), Payload(Seq.empty))
    Blockchain(Seq(genesisBlock.header), Seq(genesisBlock))
  }

  //TODO: Recover from levelDb
  def recoverBlockchain: Blockchain = genesisBlockchain
}