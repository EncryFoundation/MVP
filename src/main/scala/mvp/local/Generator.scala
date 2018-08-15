package mvp.local

import com.google.common.primitives.Longs
import mvp.data.{Input, OutputMessage, Transaction}
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import org.encryfoundation.common.crypto.{PrivateKey25519, Signature25519}
import scorex.crypto.signatures.Curve25519

object Generator {

  //Генерация транзакции, которая содержит сообщение
  def generateMessageTx(privateKey: PrivateKey25519,
                        previousMessage: Option[MessageInfo],
                        outputId: Option[Array[Byte]],
                        message: String = "Hello, world!",
                        txNum: Int,
                        salt: Array[Byte]): Transaction = {

    val messageInfo: MessageInfo = MessageInfo(
      Sha256RipeMD160(message.getBytes),
      Longs.toByteArray(System.currentTimeMillis()),
      privateKey.publicKeyBytes
    )
    val signature: Signature25519 = Signature25519(Curve25519.sign(privateKey.privKeyBytes, messageInfo.messageToSign))

    //Создание связки
    val proof: Array[Byte] = previousMessage
      .map(prevmsg => proverGenerator(prevmsg, txNum, salt))
      .getOrElse(Array.emptyByteArray)
    //Создание проверки
    val bundle: Array[Byte] = proverGenerator(messageInfo, txNum, salt)
    Transaction(
      System.currentTimeMillis(),
      outputId.map(output => Seq(Input(output, proof))).getOrElse(Seq.empty),
      Seq(
        OutputMessage(
          proof,
          bundle,
          messageInfo.message,
          messageInfo.metaData,
          messageInfo.publicKey,
          signature.signature,
          txNum - 1
        )
      )
    )
  }

  //Итеративное хеширование
  def proverGenerator(messageInfo: MessageInfo, iterCount: Int, salt: Array[Byte]): Array[Byte] =
    (0 to iterCount).foldLeft(salt) {
      case (prevHash, _) => Sha256RipeMD160(prevHash ++ messageInfo.messageToSign)
    }
}