package honey.install

import honey.config.example.HiveCellConfig
import honey.config.example.HiveConfig

/** Ok, you can load it from JSON if you want */
object Config {
  val staging = HiveConfig("conf1",
    listOf(
      HiveCellConfig("", "ip4", "publicIp1", emptyList()),
      HiveCellConfig("", "ip3", "ip2", emptyList())
    )
  )

  val dev = HiveConfig("dev",
    listOf(
      HiveCellConfig("", "192.168.31.182", "publicIp1", emptyList())
    )
  )

  val prod = staging.copy(name = "prod")
}