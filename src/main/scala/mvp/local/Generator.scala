package mvp.local

import akka.util.ByteString
import mvp.data.{Input, OutputMessage, Transaction}
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160
import org.encryfoundation.common.crypto.{PrivateKey25519, Signature25519}
import scorex.crypto.signatures.Curve25519

object Generator {

  //Генерация транзакции, которая содержит сообщение
  def generateMessageTx(privateKey: PrivateKey25519,
                        previousMessage: Option[MessageInfo],
                        outputId: Option[ByteString],
                        message: UserMessage,
                        txNum: Int,
                        salt: ByteString): Transaction = {

    val messageInfo: MessageInfo = message.toMsgInfo

    //Создание связки
    val proof: ByteString = previousMessage
      .map(prevmsg => proverGenerator(prevmsg, txNum, salt))
      .getOrElse(ByteString.empty)
    //Создание проверки
    val bundle: ByteString = proverGenerator(messageInfo, txNum, salt)
    val messageOutput: OutputMessage = OutputMessage(
      proof,
      bundle,
      messageInfo.message,
      messageInfo.metaData,
      messageInfo.publicKey,
      ByteString.empty,
      txNum - 1
    )

    val signature: Signature25519 = Signature25519(Curve25519.sign(privateKey.privKeyBytes, messageOutput.messageToSign.toArray))

    Transaction(
      System.currentTimeMillis(),
      outputId.map(output => Seq(Input(output, Seq(proof)))).getOrElse(Seq.empty),
      Seq(messageOutput.copy(signature = ByteString(signature.signature)))
    )
  }

  //Итеративное хеширование
  def proverGenerator(messageInfo: MessageInfo, iterCount: Int, salt: ByteString): ByteString =
    (0 to iterCount).foldLeft(salt) {
      case (prevHash, _) => Sha256RipeMD160(prevHash ++ messageInfo.messageToSign)
    }
}