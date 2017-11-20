package honey.config.dsl

class ScriptDSLBuilder : ObjectWithFolder<ScriptDSLBuilder> {
  override lateinit var folderPath: String

  lateinit var id: String
//  var name : String = id ?: null
  lateinit var appClass: String

  var args: String? = null
  var env: Map<String, String> = emptyMap()

  // these are actually Java Options
  var options: Set<ScriptOption> = emptySet()

  override fun build(): ScriptDSLBuilder = this

  fun jvmOpts(): String = options.joinToString(" ") { it.asArgs() }

}