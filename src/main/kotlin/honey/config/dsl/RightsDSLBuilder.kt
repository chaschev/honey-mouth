package honey.config.dsl

class RightsDSLBuilder : RightsDSL() {
  infix fun add(rights: Rights) {
    map[rights.name] = rights
  }

  fun build(): RightsDSL = this

}

