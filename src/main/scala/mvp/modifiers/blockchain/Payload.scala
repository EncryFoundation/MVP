package mvp.modifiers.blockchain

import mvp.modifiers.mempool.Transaction

case class Payload(transactions: Seq[Transaction])
