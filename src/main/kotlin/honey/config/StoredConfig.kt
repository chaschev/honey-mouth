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

  // some configs don't use hosts configurations, this field will be null
  private var activeHost: HostConfig? = null

  fun getActiveConfig(): T = activeConfig
  fun <H: HostConfig> getActiveHost(): H = activeHost as H

  fun getMyConfigFromHosts(): Pair<T, HostConfig>? {
    val myIps = getMyAddresses()

    logger.debug {"my ips: $myIps"}

    var host: HostConfig? = null

    return configs.find { config ->
      if (config !is Hosts) {
        false
      } else {
        val hosts = config.getHosts()

        val searchedHost = hosts.find { host ->
          val found = myIps.find { myIp ->
            host.getIps().contains(myIp)
          }

          found != null
        }

        if(searchedHost != null) host = searchedHost

        searchedHost != null
      }
    }?.to(host!!)
  }

  fun setActiveConfig(fromEnvironment: String) {
    val (config, hostConfig) = (if(fromEnvironment != "auto") {
      configs.find { it.name == fromEnvironment }?.to(null)
    } else {
      getMyConfigFromHosts()
    }) ?: throw Exception("couldn't determine active config from hosts or environment is not set")


    println("using config: ${config.name}, host: $hostConfig")
    activeConfig = config
    activeHost = hostConfig
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