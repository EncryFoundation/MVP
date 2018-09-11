package mvp.data

import java.security.KeyPair
import mvp.utils.ECDSAUtils._
import akka.util.ByteString

case class Wallet(unspentOutputs: Seq[Output],
                  keys: Seq[KeyPair]) {

  def updateWallet(payload: Payload): Wallet = {
    val allOutputsToSpent: Seq[ByteString] = payload.transactions.flatMap(_.inputs).map(_.useOutputId)
    val allOutputsToAdd: Seq[Output] = payload.transactions.flatMap(_.outputs).foldLeft(Seq[Output]()) {
      case (acceptableOutputs, output) => output match {
        case outputWithPk: PublicKeyContainable if keys.map(_.getPublic).contains(outputWithPk.publicKey) =>
          acceptableOutputs :+ outputWithPk
        case outputWithAddr: AddressContainable
          if keys.map(key => publicKey2Addr(key.getPublic)).contains(outputWithAddr.address) =>
          acceptableOutputs :+ outputWithAddr
        case _ => acceptableOutputs
      }
    }
    val unspentOutputsAfter: Seq[Output] = unspentOutputs
      .filter(output => !allOutputsToSpent.contains(output.id)) ++ allOutputsToAdd
    this.copy(unspentOutputsAfter)
  }

  def balance: Long = unspentOutputs.foldLeft(0L) {
    case (balance, output) => output match {
      case monetaryOutput: MonetaryOutput => balance + monetaryOutput.amount
      case _ => balance
    }
  }
}

object Wallet {

  //Add recover from db
  def recoverWallet(keys: Seq[KeyPair]): Wallet = Wallet(Seq(State.genesisOutput), keys)
}
