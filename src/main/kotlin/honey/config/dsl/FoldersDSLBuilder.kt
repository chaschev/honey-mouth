package honey.config.dsl

class FoldersDSLBuilder(val parent: InstallDSLBuilder<*>) {
  val map = HashMap<String, Folder>()

  var current: Folder = Folder(".", parent.rights().default)

  var app: Folder = current

  var lib: Folder? = null
    get() = field ?: app.copy(path = app.path + "/lib")

  var log: Folder? = null
    get() = field ?: app.copy(path = app.path + "/log")


  var bin: Folder? = null
    get() = field ?: app.copy(path = app.path + "/bin")


  infix fun String.to(value: Folder) {
    map[this] = value
  }

  fun build(): FoldersDSL = FoldersDSL(app, bin!!, lib!!, log!!, map)
}