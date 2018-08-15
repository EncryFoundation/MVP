package mvp.cli

import mvp.data.Blockchain

object Commands {

  def showHelp: Unit =
    println(
      """
Usage: [GROUP_NAME] [COMMAND] -[ARGUMENT_1]=[VAL_1] -[ARGUMENT_2]=[VAL_2]

Group name    Command          Argument       Meaning
--------------------------------------------------------------------------------
node          shutdown         None           Shutdown the node
app           help             None           Show all supported commands
blockchain    height           None           Show current blockchain height
headers       height           None           Show current headers height
sendTx        'message'        'key'          Send message
      """)

  def nodeShutdown(code: Int = 0): Nothing = sys.exit(code)

  def showCurrentBlockchainHight(blockchain: Blockchain): Unit =
    println(blockchain.blockchainHeight)

  def showCurrentHeadersHight(blockchain: Blockchain): Unit =
    println(blockchain.headersHeight)
}