package honey.config.dsl

import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail

data class FoldersDSL(
  val app: Folder,
  val bin: Folder,
  val lib: Folder,
  val log: Folder,
  val map: Map<String, Folder> = emptyMap()
) {
  fun makeDefault() {
    runBlocking {
      listOf(app, bin, lib, log, *map.values.toTypedArray()).forEach {
        it.file.mkdirsOrFail();
        it.applyRights()
      }
    }
  }
}