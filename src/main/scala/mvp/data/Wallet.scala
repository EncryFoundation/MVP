package mvp.data

case class Wallet(unspentAmountOutputs: Seq[OutputAmount],
                  unspentMessageOutputs: Seq[OutputMessage],
                  unspentPKIOutputs: Seq[OutputPKI])

object Wallet {

  //Add recover from db
  def recoverWallet: Wallet = Wallet(Seq.empty, Seq.empty, Seq.empty)
}
