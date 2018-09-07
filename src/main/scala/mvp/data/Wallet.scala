package mvp.data

import java.security.KeyPair

import akka.util.ByteString

case class Wallet(unspentAmountOutputs: Seq[OutputAmount],
                  unspentMessageOutputs: Seq[OutputMessage],
                  unspentPKIOutputs: Seq[OutputPKI],
                  keys: Seq[KeyPair]) {

  def updateWallet(payload: Payload): Wallet = {
    val allOutputsToSpent: Seq[ByteString] = payload.transactions.flatMap(_.inputs).map(_.useOutputId)
    val allAmountOutputsToAdd: Seq[OutputAmount] =
      payload.transactions.flatMap(_.outputs)
        .filter(_.isInstanceOf[OutputAmount])
        .map(_.asInstanceOf[OutputAmount])
        .filter(output => keys.map(_.getPublic).contains(output.publicKey))
    val unspentMessageOutputsToAdd: Seq[OutputMessage] =
      payload.transactions.flatMap(_.outputs)
        .filter(_.isInstanceOf[OutputMessage])
        .map(_.asInstanceOf[OutputMessage])
        .filter(output => keys.map(_.getPublic).contains(output.publicKey))
    val unspentPKIOutputsToAdd: Seq[OutputPKI] =
      payload.transactions.flatMap(_.outputs)
        .filter(_.isInstanceOf[OutputPKI])
        .map(_.asInstanceOf[OutputPKI])
        .filter(output => keys.map(_.getPublic).contains(output.publicKey))
    val unspentAmountOutputsAfter: Seq[OutputAmount] = unspentAmountOutputs
      .filter(output => !allOutputsToSpent.contains(output.id)) ++ allAmountOutputsToAdd
    val unspentMessageOutputsAfter: Seq[OutputMessage] = unspentMessageOutputs
      .filter(output => !allOutputsToSpent.contains(output.id)) ++ unspentMessageOutputsToAdd
    val unspentPKIOutputsAfter: Seq[OutputPKI] = unspentPKIOutputs
      .filter(output => !allOutputsToSpent.contains(output.id)) ++ unspentPKIOutputsToAdd
    this.copy(unspentAmountOutputsAfter, unspentMessageOutputsAfter, unspentPKIOutputsAfter)
  }
}

object Wallet {

  //Add recover from db
  def recoverWallet(keys: Seq[KeyPair]): Wallet = Wallet(Seq(State.genesisOutput), Seq.empty, Seq.empty, keys)
}
