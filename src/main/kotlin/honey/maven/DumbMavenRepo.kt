package honey.maven

class DumbMavenRepo(
  val root: String

) : MavenRepo {
  override fun root() = root

  override fun resolveUrl(group: String, module: String, version: String): String =
    root + "/" + group.replace('.', '/') + "/" + module + "/" + version + "/" + file(module, version)

}