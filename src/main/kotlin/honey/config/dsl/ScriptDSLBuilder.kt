package honey.config.dsl

class ScriptDSLBuilder : ObjectWithFolder<ScriptDSLBuilder> {
  override lateinit var folderPath: String

  lateinit var id: String
  var name = id
  lateinit var appClass: String

  var args: String? = null
  var env: Map<String, String> = emptyMap()
  var options: Set<ScriptOption> = emptySet()

  override fun build(): ScriptDSLBuilder = this

}