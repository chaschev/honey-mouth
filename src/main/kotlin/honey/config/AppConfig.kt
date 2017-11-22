package honey.config

interface AppConfig {
  val name: String
  fun init(): Unit
}

interface Hosts {
  fun getAllHosts(): Set<String>
}

