package honey.config.dsl

import java.io.File

data class Folder(
  val path: String,
  val rights: Rights = Rights.omit
) {
  val file by lazy { File(path) }
  suspend fun applyRights() {
    rights.apply(file)
  }
}