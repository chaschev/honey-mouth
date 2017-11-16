package honey.config.dsl

data class User(
  val name: String,
  val group: String
) {
  companion object {
    val omit = User("omit", "")
  }
}