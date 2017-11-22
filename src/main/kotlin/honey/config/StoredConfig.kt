package honey.config

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

  fun tryGetMyConfig(): T?{
    val myIps = getMyAddresses()

    return configs.find { config ->
      (null != if (config !is Hosts) {
        null
      } else {
        val hosts = config.getAllHosts()

        myIps.find { hosts.contains(it) }
      })
    }
  }

  companion object {
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