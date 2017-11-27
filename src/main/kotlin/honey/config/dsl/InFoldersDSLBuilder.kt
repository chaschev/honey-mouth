package honey.config.dsl

import honey.install.exec
import kotlinx.coroutines.experimental.runBlocking

class InFoldersDSLBuilder(
  override var folderPath: String,
  val parent: InstallDSLBuilder<*>
) : ObjectWithFolder<InFoldersDSLBuilder> {

  var linkAllScripts = false
  val ids: ArrayList<String> = ArrayList()

  fun script(builder: ScriptDSLBuilder.() -> Unit): ScriptDSLBuilder {
    val script = ScriptDSLBuilder(folderPath, parent).apply(builder).build()

    parent.scripts.add(script)

    return script
  }

  fun updateScript(appClass: String, builder: (UpdateScriptDSLBuilder.() -> Unit)? = null): UpdateScriptDSLBuilder {
    val temp = UpdateScriptDSLBuilder(folderPath, parent, appClass)

    parent.scripts.add(temp)

    if(builder == null) {
      return temp
    }

    return temp.apply(builder).build()
  }

  fun linkAllScripts() {
    linkAllScripts = true
  }


  fun link(id: String) {
    if (parent.script(id) == null) {
      throw Exception("scripts id not found: $id")
    }

    ids.add(id)
  }

  override fun build(): InFoldersDSLBuilder {
    return this
  }

  fun linkScripts() {
    if(linkAllScripts) {
      parent.scripts.forEach {
        linkScript(it.id)
      }
    }

    ids.forEach {linkScript(it)}
  }

  private fun linkScript(id: String) {
    runBlocking {
      println("linking ${parent.script(id)!!.folderPath}/$id from $folderPath/$id")
      "rm $folderPath/$id".exec(1000)
      "ln -s ${parent.script(id)!!.file().absoluteFile.canonicalPath} $folderPath/$id".exec(1000, inheritIO = true)
    }
  }
}