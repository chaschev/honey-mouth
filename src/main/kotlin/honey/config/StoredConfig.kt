package honey.config

import mu.KLogging
import java.net.NetworkInterface
import java.util.*

data class StoredConfig<T : AppConfig>(
  val appName: String,
  val version: String,
  val revision: String,
  val build: Long? = null,
  val buildTime: Date,
  val team: String,
  val configs: List<T>
) {

  private lateinit var activeConfig: T

  fun getActiveConfig(): T = activeConfig

  fun getMyConfigFromHosts(): T? {
    val myIps = getMyAddresses()

    logger.debug {"my ips: $myIps"}

    return configs.find { config ->
      (null != if (config !is Hosts) {
        null
      } else {
        val hosts = config.getAllHosts()

        myIps.find {
          val r = hosts.contains(it)
          if(r) logger.debug { "matched ip: $it" }
          r
        }
      })
    }
  }

  fun setActiveConfig(fromEnvironment: String) {
    val config = (if(fromEnvironment != "auto") {
      configs.find { it.name == fromEnvironment }
    } else {
      getMyConfigFromHosts()
    }) ?: throw Exception("couldn't determine active config from hosts or environment is not set")


    println("using config: ${config.name}")
    activeConfig = config
  }

  companion object : KLogging() {
      fun getMyAddresses(): List<String> {
        val interfaces = NetworkInterface.getNetworkInterfaces().asSequence()

        return interfaces
          .filter { !it.isLoopback && it.isUp }
          .flatMap { it.inetAddresses.asSequence() }
          .map { it.hostAddress }
          .toList()
      }
  }
}