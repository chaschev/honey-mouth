package honey.config.dsl

import mu.KLogging

class UsersDSLBuilder {
  companion object : KLogging()

  var default: User = User.omit

  val list = ArrayList<User>()

  infix fun String.inGroup(value: String) {
    logger.debug { "add user $this" }
    list.add(User(this, value))
  }

  fun build(): Users = Users(default, list)
}