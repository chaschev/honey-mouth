package honey.config.dsl

import honey.config.AppConfig
import honey.config.StoredConfig
import honey.install.HoneyMouthOptions

class InstallDSLBuilder<C : AppConfig> {
  lateinit var installOptions: HoneyMouthOptions<C>

  var requiredVersions: RequireDSLBuilder? = null
  internal var before: (() -> Unit)? = null
  internal var after: (() -> Unit)? = null
  internal var updateApp: (() -> Unit)? = null
  private var users: Users? = null
  private var rights: RightsDSL? = null
  internal lateinit var folders: FoldersDSL
  internal var inFolders: ArrayList<InFoldersDSLBuilder> = ArrayList()

  var app: AppDSLBuilder<C>? = null

  lateinit var config: StoredConfig<C>

  val scripts = ArrayList<ScriptDSLBuilder>()

  fun build(): InstallDSLBuilder<C> = this

  fun before(block: () -> Unit) {
    before = block
  }

  fun config(block: () -> StoredConfig<C>) {
    config = block()
  }

  fun require(builder: RequireDSLBuilder.() -> Unit) {
    requiredVersions = RequireDSLBuilder().apply(builder)
  }

  fun after(block: () -> Unit) {
    after = block
  }

  fun updateApp(block: () -> Unit) {
    updateApp = block
  }

  fun users() = users!!

  fun users(name: String): User {
    try {
      return users!![name]!!
    } catch (e: Exception) {
      println("user not found: $name. Available users: ${users?.list}")
      throw e
    }
  }

  fun users(builder: UsersDSLBuilder.() -> Unit) {
    this.users = UsersDSLBuilder().apply(builder).build()
  }

  fun rights() = rights!!
  fun rights(name: String) = rights!![name]!!

  fun rights(builder: RightsDSLBuilder.() -> Unit) {
    this.rights = RightsDSLBuilder().apply(builder).build()
  }

  fun app() = app!!

  fun app(builder: AppDSLBuilder<C>.() -> Unit) {
    this.app = AppDSLBuilder(installOptions.installer).apply(builder)
  }

  fun folders() = folders

  fun folders(name: String) = folders.map[name]!!

  fun folders(builder: FoldersDSLBuilder.() -> Unit) {
    this.folders = FoldersDSLBuilder(this).apply(builder).build()
  }

  fun inFolder(path: String, builder: InFoldersDSLBuilder.() -> Unit) {
    inFolders.add(
      InFoldersDSLBuilder(path,this).apply(builder).build()
    )
  }

  fun inFolder(folder: Folder, builder: InFoldersDSLBuilder.() -> Unit)
    = inFolder(folder.path, builder)

  companion object {
    fun <C : AppConfig> build(builder: InstallDSLBuilder<C>.() -> Unit):
      InstallDSLBuilder<C> =
      InstallDSLBuilder<C>().apply(builder).build()
  }

  fun script(id: String): ScriptDSLBuilder? {
    return scripts.find { it.id == id }
  }

}


