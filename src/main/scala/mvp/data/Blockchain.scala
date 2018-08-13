package mvp.data

//TODO: Blocks to levelDb

case class Blockchain(headers: Seq[Header] = Seq.empty, blocks: Seq[Block] = Seq.empty) {

  val headersHeight: Int = headers.lastOption.map(_.height).getOrElse(Blockchain.genesisHeight)

  val blockchainHeight: Int = blocks.lastOption.map(_.header.height).getOrElse(Blockchain.genesisHeight)

  val lastBlock: Option[Block] = blocks.lastOption

  def addPayload(payload: Payload): Blockchain =
    Blockchain(
      headers,
      headers.find(_.merkleTreeRoot sameElements payload.id)
        .map(header => blocks :+ Block(header, payload))
        .getOrElse(blocks)
    )

  def addHeader(headerToAdd: Header): Blockchain = Blockchain(headers :+ headerToAdd, blocks)

  def getHeaderAtHeight(height: Int): Option[Header] = headers.find(_.height == height)
}

object Blockchain {

  val genesisHeight: Int = -1

  val genesisBlockchain: Blockchain = {
    val genesisBlock: Block =
      Block(Header(1L, 0, Array.emptyByteArray, Array.emptyByteArray, Array.emptyByteArray), Payload(Seq.empty))
    Blockchain(Seq(genesisBlock.header), Seq(genesisBlock))
  }

  //TODO: Recover from levelDb
  def recoverBlockchain: Blockchain = genesisBlockchain
}