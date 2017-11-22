package honey.config.example


//@JsonRootName("hive")
data class HiveConfig(
  val name: String,
  val cells: List<HiveCellConfig>,
  val defaultCellWorkload: Int = 100
) {
  operator fun get(name: String): HiveCellConfig = cells.find { it.name == name }!!
  fun byLabel(label: String): List<HiveCellConfig> = cells.filter { it.labels.contains(label) }
}