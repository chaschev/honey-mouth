package honey.config

import honey.config.dsl.InstallDSLBuilder
import honey.config.dsl.ReleaseDSLDef
import honey.install.AppInstaller
import honey.install.HoneyMouthOptions
import kotlin.reflect.KClass


interface AppConfig {
  val name: String
  fun init(): Unit

  companion object {
    fun <C: AppConfig, T: ReleaseDSLDef<C>>fromEnvironment(
      releaseDefClass: KClass<T>,
      configClass: KClass<C>,
      environment: String = "auto",
      devJar: String? = null
    ): InstallDSLBuilder<C> {
      val releaseDef = releaseDefClass
        .constructors.first().call(devJar)

      val options = HoneyMouthOptions(releaseDef)

      return AppInstaller.dsl(options)
    }
  }
}

