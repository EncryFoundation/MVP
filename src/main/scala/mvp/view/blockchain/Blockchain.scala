package mvp.view.blockchain

import mvp.modifiers.blockchain.Block

//TODO: Blocks to levelDb

case class Blockchain(blocks: Seq[Block]) {

  def addBlock(blockToAdd: Block): Blockchain = Blockchain(blocks :+ blockToAdd)
}

object Blockchain {

  val emptyBlockchain: Blockchain = Blockchain(Seq.empty)

  //TODO: Recover from levelDb
  def recoverBlockchain: Blockchain = emptyBlockchain
}