package honey.config.dsl

class AppDSLBuilder {
  fun resources(builder: ResourcesDSLBuilder.() -> Unit): ResourcesDSLBuilder {
    return ResourcesDSLBuilder().apply(builder)
  }

  class ResourcesDSLBuilder{
    val list = ArrayList<Pair<String, Folder>>()

    infix fun String.into(folder: Folder) {
      list.add(Pair(this, folder))
    }
  }
}

class RequireDSLBuilder{
  val list = ArrayList<Pair<String, String>>()

  infix fun String.version(versionTemplate: String) {
    list.add(Pair(this, versionTemplate))
  }
}
