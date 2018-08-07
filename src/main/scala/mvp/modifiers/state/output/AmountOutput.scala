package mvp.modifiers.state.output

import com.google.common.primitives.Longs
import mvp.modifiers.mempool.Transaction.Address
import mvp.utils.Crypto.Sha256RipeMD160

case class AmountOutput(address: Address,
                        amount: Long) extends Output {

  override def unlock(proof: Array[Byte]): Boolean = ???

  override val id: Array[Byte] = Sha256RipeMD160(address ++ Longs.toByteArray(amount))
}
