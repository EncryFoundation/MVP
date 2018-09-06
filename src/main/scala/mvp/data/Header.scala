package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.BlockchainUtils._
import mvp.utils.EncodingUtils

case class Header(timestamp: Long,
                  height: Int,
                  previousBlockHash: ByteString,
                  minerSignature: ByteString,
                  merkleTreeRoot: ByteString) extends Modifier {

  val messageToSign: ByteString =
    toByteString(timestamp) ++ toByteString(height) ++ previousBlockHash ++ merkleTreeRoot


  override val id: ByteString = Sha256RipeMD160(
    toByteString(timestamp) ++ toByteString(height) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot
  )
}