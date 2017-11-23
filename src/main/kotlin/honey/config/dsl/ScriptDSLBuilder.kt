package honey.config.dsl

import honey.install.AppInstaller
import honey.install.UnixStartScript
import java.io.File

class UpdateScriptDSLBuilder(
  override var folderPath: String,
  override val dsl: InstallDSLBuilder<*>
) : ScriptDSLBuilder(folderPath, dsl) {
  init {
    id = "update-${dsl.config.appName}"
    appClass = AppInstaller.javaClass.name
    env = mapOf(
      INSTALLATION_PATH to File(".").absolutePath
    )
  }

  override fun build(): UpdateScriptDSLBuilder = this

  companion object {
    val INSTALLATION_PATH = "INSTALLATION_PATH"
  }
}

open class ScriptDSLBuilder(
  override var folderPath: String,
  open val dsl: InstallDSLBuilder<*>
) : ObjectWithFolder<ScriptDSLBuilder> {

  lateinit var id: String
//  var name : String = id ?: null
  lateinit var appClass: String

  var args: String? = null
  var env: Map<String, String> = emptyMap()

  // these are actually Java Options
  var options: Set<ScriptOption> = emptySet()

  override fun build(): ScriptDSLBuilder = this

  fun jvmOpts(): String = options.joinToString(" ") { it.asArgs() }

  fun file() = File(folderPath, id)

  suspend fun writeScript() {
    val scriptFile = file()
    scriptFile.writeText(
      UnixStartScript(
        dsl.config.appName,
        "..",  //TODO fix appHomePath from app
        "APP_OPTS",
        jvmOpts(),
        "${dsl.folders.lib.file.absoluteFile.path}/*",
        "",
        appClass,
        env
      ).script())

    Rights.executableAll.apply(scriptFile)
  }
}