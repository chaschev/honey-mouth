package honey.config.dsl

import honey.config.DataVolume

sealed class ScriptOption {
  data class jvmOpts(val args: String) : ScriptOption() {
    override fun asArgs(): String = args
  }

  data class memory(val min: DataVolume? = null, val max: DataVolume? = null) : ScriptOption() {
    override fun asArgs(): String {
      return (if(min != null) "-Xms${min!!.forJava()}" else "") + " " +
        (if(max != null) "-Xms${max!!.forJava()}" else "")
    }
  }

  fun DataVolume.forJava() = "$value${this.javaClass.simpleName.first()}"

  abstract fun asArgs(): String
}