package honey.config.example

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*
import honey.config.AppConfig
import honey.config.Hosts
import mu.KLogging
import java.io.File

data class HiveConfigs(
  override val name: String,

  val configs: List<HiveConfig>
) : AppConfig, Hosts {

  override fun init() {

  }

  constructor(name: String, vararg configs: HiveConfig)
    : this(name, configs.toList())

  init {
    postInit()
  }

  fun postInit() {
    for (hive in configs)
      for (cell in hive.cells)
        cell.parent = hive
  }

  operator fun get(env: String): HiveConfig? =
    configs.find { it.name == env }


  override fun getAllHosts(): Set<String> {
    return configs
      .asSequence()
      .flatMap { it.cells.asSequence()
        .flatMap { sequenceOf(it.name, it.ip, it.publicIp).filterNotNull() }
    }.toSet()
  }
}


