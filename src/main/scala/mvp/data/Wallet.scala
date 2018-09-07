package mvp.data

import java.security.KeyPair

import akka.util.ByteString

case class Wallet(unspentOutputs: Seq[Output],
                  keys: Seq[KeyPair]) {

  def updateWallet(payload: Payload): Wallet = {
    val allOutputsToSpent: Seq[ByteString] = payload.transactions.flatMap(_.inputs).map(_.useOutputId)
    val allOutputsToAdd: Seq[Output] =
      payload.transactions.flatMap(_.outputs)
        .filter(_.isInstanceOf[PublicKeyContainable])
        .filter(output => keys.map(_.getPublic).contains(output.asInstanceOf[PublicKeyContainable].publicKey))
    val unspentOutputsAfter: Seq[Output] = unspentOutputs
      .filter(output => !allOutputsToSpent.contains(output.id)) ++ allOutputsToAdd
    this.copy(unspentOutputsAfter)
  }
}

object Wallet {

  //Add recover from db
  def recoverWallet(keys: Seq[KeyPair]): Wallet = Wallet(Seq(State.genesisOutput), keys)
}
