package honey.config.dsl

import honey.config.dsl.Rights.omit

open class RightsDSL {
  var default: Rights = omit
  val map = HashMap<String, Rights>()
  operator fun get(name: String): Rights = map[name]!!
}