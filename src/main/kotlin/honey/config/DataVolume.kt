package honey.config

sealed class DataVolume(open val value: Long){
  abstract fun toBytes(): Long

  override fun toString(): String =
    "$value${this::class.simpleName!!.toLowerCase()}"

  class KB(override val value: Long): DataVolume(value) {
    override fun toBytes(): Long  = value * 1024
  }

  class MB(override val value: Long): DataVolume(value) {
    override fun toBytes(): Long  = value * 1024 * 1024
  }

  class GB(override val value: Long): DataVolume(value) {
    override fun toBytes(): Long  = value * 1024 * 1024 * 1024
  }
}