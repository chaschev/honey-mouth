package honey.config.example


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import honey.config.example.HiveConfig as config
import honey.config.example.HiveConfigs as configs
import honey.config.example.HiveCellConfig as cell

fun main(args: Array<String>) {
  val mapper = jacksonObjectMapper()
  val hiveConfigs = configs("dev",
    config("hc1",
      listOf(
        cell("c1", "ip", "publicIp",
          listOf("db", "app")))
    )
  )

  hiveConfigs["hc1"]!!["c1"].parent = hiveConfigs["hc1"]

  val s = mapper.writeValueAsString(
    hiveConfigs)

  println(s)

  val configs = mapper.readValue<configs>(s)

  println(configs)
}