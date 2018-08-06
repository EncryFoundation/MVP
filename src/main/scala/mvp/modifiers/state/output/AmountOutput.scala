package mvp.modifiers.state.output

import mvp.modifiers.mempool.Transaction.Address

case class AmountOutput(address: Address,
                        amount: Long)
