package mvp.modifiers.blockchain

case class Header(timestamp: Long,
                  previousBlockHash: Array[Byte],
                  minerSignature: Array[Byte],
                  payloadHash: Array[Byte])
