package mvp.local

import java.security.PrivateKey
import akka.util.ByteString
import mvp.crypto.ECDSA
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.data.{Input, OutputMessage, Transaction}
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
import scala.util.Random

object Generator {

  //Генерация транзакции, которая содержит сообщение
  def generateMessageTx(privateKey: PrivateKey,
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
      txNum - 1,
      //Nonce shouldn't be random, should generate from tx id
      Random.nextLong()
    )

    val signature: ByteString = ECDSA.sign(privateKey, messageOutput.messageToSign)

    Transaction(
      System.currentTimeMillis(),
      outputId.map(output => Seq(Input(output, Seq(proof)))).getOrElse(Seq.empty),
      Seq(messageOutput.copy(signature = signature))
    )
  }

  //Итеративное хеширование
  def proverGenerator(messageInfo: MessageInfo, iterCount: Int, salt: ByteString): ByteString =
    (0 to iterCount).foldLeft(salt) {
      case (prevHash, _) => Sha256RipeMD160(prevHash ++ messageInfo.messageToSign)
    }
}