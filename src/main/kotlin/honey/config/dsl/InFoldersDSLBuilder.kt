package honey.config.dsl

class InFoldersDSLBuilder(val parent: InstallDSLBuilder<*>) {
  private val list = ArrayList<ObjectWithFolder<*>>()

  lateinit var folderPath: String

  val links = Links()

  fun folder(folderPath: String): InFoldersDSLBuilder {
    this.folderPath = folderPath; return this
  }

  fun script(builder: ScriptDSLBuilder.() -> Unit): ScriptDSLBuilder {
    val script = ScriptDSLBuilder().apply(builder).build()

    list.add(script)

    return script
  }

  data class Links(
    val ids: ArrayList<String> = ArrayList(),
    var linkAllScripts: Boolean = false
  )

  fun linkAllScripts() {
    links.linkAllScripts = true
  }


  fun link(id: String) {
    if (parent.scripts.find { it.id == id } == null) {
      throw Exception("scripts id not found: $id")
    }

    links.ids.add(id)
  }

  fun build(): ArrayList<ObjectWithFolder<*>> {
    list.forEach { it.folderPath = folderPath }

    parent.sortInFoldersOut(list)

    return list
  }
}