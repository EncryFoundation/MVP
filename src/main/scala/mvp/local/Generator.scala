package mvp.local

import java.security.{PrivateKey, PublicKey}
import akka.util.ByteString
import mvp.crypto.ECDSA
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.data._
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
import scala.util.Random
import mvp.utils.ECDSAUtils._

object Generator {

  //Генерация транзакции, которая содержит сообщение
  def generateMessageTx(privateKey: PrivateKey,
                        previousMessage: Option[MessageInfo],
                        outputId: Option[ByteString],
                        message: UserMessage,
                        txNum: Int,
                        salt: ByteString,
                        fee: Long,
                        boxesToFee: Seq[MonetaryOutput],
                        publicKey: PublicKey): Transaction = {

    val charge: Long = boxesToFee.map(_.amount).sum - fee
    val inputs: Seq[Input] = boxesToFee.map(box => Input(box.id, Seq.empty))
    val outputs: Seq[OutputAmount] = {
      //Nonce should't be random, only generation from tx id
      if (charge > 0) Seq(OutputAmount(publicKey2Addr(publicKey), charge, Random.nextLong()))
      else Seq.empty
    }

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

    val unsignedTx = Transaction(
      System.currentTimeMillis(),
      fee,
      outputId.map(output => Seq(Input(output, Seq(proof)))).getOrElse(Seq.empty) ++ inputs,
      Seq(messageOutput.copy(signature = signature)) ++ outputs
    )
    val sing: ByteString = ECDSA.sign(privateKey, unsignedTx.messageToSign)
    val signedPaymentInputs: Seq[Input] = boxesToFee.map(box =>
      Input(box.id, Seq(sing, ECDSA.compressPublicKey(publicKey)))
    )
    unsignedTx
      .copy(inputs = outputId.map(output => Seq(Input(output, Seq(proof)))).getOrElse(Seq.empty) ++
        signedPaymentInputs
      )
  }

  //Итеративное хеширование
  def proverGenerator(messageInfo: MessageInfo, iterCount: Int, salt: ByteString): ByteString =
    (0 to iterCount).foldLeft(salt) {
      case (prevHash, _) => Sha256RipeMD160(prevHash ++ messageInfo.messageToSign)
    }
}