package honey.config.dsl

data class Users(
  val default: User,
  private val list: ArrayList<User>
) {
  operator fun get(name: String): User? = list.find { it.name == name }
}