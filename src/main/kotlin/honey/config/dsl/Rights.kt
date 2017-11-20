package honey.config.dsl

import honey.install.exec
import java.io.File


sealed class Rights(open val name: String) {

  object omit : Rights("omit") {
    override suspend fun apply(file: File) {}
  }

  abstract suspend fun apply(file: File)

  data class UserRights(
    override val name: String,
    val access: String,
    val owner: User = User.omit,
    val recursive: Boolean = true
  ) : Rights(name) {

    override suspend fun apply(file: File) {
      "chmod ${if(recursive) "-R" else ""} $access ${file.path}".exec(1000)
      owner.apply(file)
    }

    fun noRecurse() = copy(recursive = false)

    companion object {
      val readOnly = UserRights("readOnly", "a=rx")
      val all = UserRights("all", "a=rwx")
      val writeProtect = UserRights("writeProtect", "u=rwx,go=rx")
    }
  }
}