package honey.config.example


import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("hiveCell")
data class HiveCellConfig(
  @JsonBackReference
  val name: String,
  val ip: String,
  val publicIp: String?,
  val labels: List<String>,
  var workload: Int = 100
) {
  var parent: HiveConfig? = null

  override fun toString(): String {
    return """HiveCellConfig($name, int: $ip, ext: $publicIp, [${labels.joinToString()}], $workload"""
  }

}

