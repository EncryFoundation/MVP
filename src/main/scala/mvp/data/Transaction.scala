package mvp.data

import akka.util.ByteString
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils.toByteArray

case class Transaction(timestamp: Long,
                       inputs: Seq[Input],
                       outputs: Seq[Output]) extends Modifier {

  override val id: ByteString = Sha256RipeMD160(
    toByteArray(timestamp) ++ inputs.foldLeft(ByteString.empty)(_ ++ _.id) ++ outputs.foldLeft(ByteString.empty)(_ ++ _.id)
  )
}
