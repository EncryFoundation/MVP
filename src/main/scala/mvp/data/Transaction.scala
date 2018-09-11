package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.BlockchainUtils.toByteString

case class Transaction(timestamp: Long,
                       fee: Long,
                       inputs: Seq[Input],
                       outputs: Seq[Output]) extends Modifier {

  val messageToSign: ByteString = Sha256RipeMD160(
    toByteString(timestamp) ++
    inputs.flatMap(_.useOutputId) ++
    outputs.flatMap(_.messageToSign)
  )

  override val id: ByteString = Sha256RipeMD160(
    toByteString(timestamp) ++ inputs.foldLeft(ByteString.empty)(_ ++ _.id) ++ outputs.foldLeft(ByteString.empty)(_ ++ _.id)
  )
}
