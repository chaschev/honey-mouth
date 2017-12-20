package honey.config.example


import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonRootName
import honey.config.HostConfig

@JsonRootName("hiveCell")
data class HiveCellConfig(
  @JsonBackReference
  val name: String,
  val ip: String,
  val publicIp: String?,
  val labels: List<String>,
  var workload: Int = 100
) : HostConfig {
  override fun getIps(): List<String> {
    return listOfNotNull(ip, publicIp)
  }

  var parent: HiveConfig? = null

  override fun toString(): String {
    return """HiveCellConfig($name, int: $ip, ext: $publicIp, [${labels.joinToString()}], $workload"""
  }

}

