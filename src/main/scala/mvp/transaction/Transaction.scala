package mvp.transaction

case class Transaction(bundle: Array[Byte],
                       check: Array[Byte],
                       publicKeyHash: Array[Byte],
                       userData: Array[Byte],
                       publicKey: Array[Byte],
                       signature: Array[Byte])
