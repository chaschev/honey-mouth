package honey.config.dsl


sealed class Rights(open val name: String) {

  object omit : Rights("omit")

  data class UserRights(
    override val name: String,
    val access: String,
    val recursive: Boolean = true
  ) : Rights(name) {
    fun noRecurse() = copy(recursive = false)

    companion object {
      val readOnly = UserRights("readOnly", "a=rx")
      val all = UserRights("all", "a=rwx")
      val writeProtect = UserRights("writeProtect", "u=rwx,go=rx")
    }
  }
}