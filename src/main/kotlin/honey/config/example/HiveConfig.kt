package honey.config.example

import honey.config.AppConfig
import honey.config.Hosts


//@JsonRootName("hive")
data class HiveConfig(
  override val name: String,
  val cells: List<HiveCellConfig>,
  val defaultCellWorkload: Int = 100
) : AppConfig, Hosts {

  operator fun get(name: String): HiveCellConfig = cells.find { it.name == name }!!
  fun byLabel(label: String): List<HiveCellConfig> = cells.filter { it.labels.contains(label) }

  override fun getAllHosts(): Set<String> {
    return cells.asSequence()
        .flatMap { sequenceOf(it.name, it.ip, it.publicIp).filterNotNull() }
      .toSet()
  }

  override fun init() {
  }

}