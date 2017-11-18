import honey.config.example.HiveConfigs as configs
import honey.config.example.HiveConfig as config
import honey.config.example.HiveCellConfig as cell

object Config {
  val staging = configs(
    "staging",
    config("conf1",
      listOf(
        cell("", "ip4", "publicIp1", emptyList()),
        cell("", "ip3", "ip2", emptyList())
      )
    ),
    config("conf2",
      listOf(
        cell("", "ip4", "publicIp2", emptyList()),
        cell("", "ip3", "ip2", emptyList())
      )
    )
  )

  val dev = staging.copy(name = "dev")
  val prod = staging.copy(name = "prod")
}