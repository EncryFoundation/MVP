package mvp.local

import com.google.common.primitives.Longs
import mvp.local.messageTransaction.MessageInfo
import mvp.modifiers.mempool.Transaction
import mvp.modifiers.state.input.Input
import mvp.modifiers.state.output.MessageOutput
import mvp.utils.Crypto.Sha256RipeMD160
import org.encryfoundation.common.crypto.{PrivateKey25519, Signature25519}
import scorex.crypto.signatures.Curve25519

object Generator {

  val iterCount: Int = 10

  def generateMessageTx(privateKey: PrivateKey25519,
                        previousMessage: Option[MessageInfo],
                        outputId: Option[Array[Byte]]): Transaction = {

    val messageInfo: MessageInfo = MessageInfo(
      "Hello, world!".getBytes,
      Longs.toByteArray(System.currentTimeMillis()),
      privateKey.publicKeyBytes
    )
    val signature: Signature25519 = Signature25519(Curve25519.sign(privateKey.privKeyBytes, messageInfo.messageToSign))

    //first field
    val proof: Array[Byte] = previousMessage
      .map(proverGenerator(_, iterCount - 1, privateKey.privKeyBytes))
      .getOrElse(Array.emptyByteArray)
    //second field
    val bundle: Array[Byte] = proverGenerator(messageInfo, iterCount, privateKey.privKeyBytes)
    Transaction(
      System.currentTimeMillis(),
      outputId.map(output => Seq(Input(output, proof))).getOrElse(Seq.empty),
      Seq(
        MessageOutput(
          proof,
          bundle,
          messageInfo.message,
          messageInfo.metaData,
          messageInfo.publicKey,
          signature.signature
        )
      )
    )
  }

  def proverGenerator(messageInfo: MessageInfo, iterCount: Int, salt: Array[Byte]): Array[Byte] =
    (0 to iterCount).foldLeft(salt) {
      case (prevHash, i) => Sha256RipeMD160(prevHash ++ messageInfo.messageToSign)
    }
}
