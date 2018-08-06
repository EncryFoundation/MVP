package mvp.view.blockchain.validator

import mvp.view.blockchain.validator.block.{BlockValidator, HeaderValidator, PayloadValidator}
import mvp.view.blockchain.validator.transaction.TransactionValidator

trait ModifiersValidator extends BlockValidator
                        with HeaderValidator
                        with PayloadValidator
                        with TransactionValidator
