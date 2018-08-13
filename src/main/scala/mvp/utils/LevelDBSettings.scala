package mvp.utils

case class LevelDBSettings(enable: Boolean,
                           recoverMode: Boolean,
                           batchSize: Int)
