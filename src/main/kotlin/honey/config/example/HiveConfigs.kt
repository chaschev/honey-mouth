package honey.config.example

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*
import honey.config.AppConfig
import mu.KLogging
import java.io.File

//@JsonRootName("hives")
data class HiveConfigs(
  override val name: String,

  @JsonProperty("hives")
  val configs: List<HiveConfig>
) : AppConfig {

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

  fun toJson(): String = mapper.writeValueAsString(this)

  companion object : KLogging(){
    private val jsonFactory = JsonFactory()
    val mapper: ObjectMapper

    init {
      jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
      mapper = ObjectMapper(jsonFactory).registerKotlinModule()
    }

    fun fromFile(path: String): HiveConfigs = mapper.readValue(File(path))

    //environment must always be set. if not set, fail. don't default to local

    val CURRENT: HiveConfig by lazy {
      val env = Env["ACTIVE_HIVE"]
        ?: Env["DEV_HIVE"]
        ?: throw Exception("environment variable ACTIVE_HIVE is not set!")

      val configs = fromFile(configPath())

      val config = configs[env]
        ?: throw Exception("HiveConfig $env not found. Available configs are: ${configs.configs.map { it.name }}")

      logger.info { "loaded hive config: $config" }

      return@lazy config
    }

    val honeyBadgerPath: String by lazy {
      TODO()
//            FileUtils.mkdirsOrThrow(
//                Env["HONEY_HOME"]
//                    ?: Env["HONEY_DEV_HOME"]
//                    ?: "/etc/honey")
    }

    fun configPath(): String = honeyBadgerPath + "/hives.json"
  }

}


