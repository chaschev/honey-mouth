package honey.config

interface AppConfig {
  val name: String
  fun init(): Unit
}

class ConfigManager<T : AppConfig> {

}


