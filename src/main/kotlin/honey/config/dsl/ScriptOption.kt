package honey.config.dsl

import honey.config.DataVolume

sealed class ScriptOption {
  data class jvmOpts(val args: String) : ScriptOption()
  data class memory(val min: DataVolume? = null, val max: DataVolume? = null) : ScriptOption()
}