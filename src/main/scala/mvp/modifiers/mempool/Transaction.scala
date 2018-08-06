package mvp.modifiers.mempool

import mvp.modifiers.Modifier

trait Transaction extends Modifier

object Transaction {

  type Address = Array[Byte]
}
