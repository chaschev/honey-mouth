package honey.config

import java.util.*

data class StoredConfig<T : AppConfig>(
  val appName: String,
  val version: String,
  val revision: String,
  val build: Long? = null,
  val buildTime: Date,
  val team: String,
  val configs: List<T>)