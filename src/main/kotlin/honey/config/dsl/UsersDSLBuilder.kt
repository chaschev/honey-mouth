package honey.config.dsl

class UsersDSLBuilder {
  var default: User = User.omit

  val list = ArrayList<User>()

  infix fun String.`in`(value: String) {
    list.add(User(this, value))
  }

  fun build(): Users = Users(default, list)
}