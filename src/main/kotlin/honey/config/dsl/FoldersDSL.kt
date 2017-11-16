package honey.config.dsl

data class FoldersDSL(
  val app: Folder,
  val bin: Folder,
  val lib: Folder,
  val log: Folder,
  val map: Map<String, Folder> = emptyMap()
)