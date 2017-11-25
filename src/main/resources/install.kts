import honey.config.MB
import honey.config.StoredConfig
import honey.config.dsl.Folder
import honey.config.dsl.InstallDSLBuilder.Companion.build
import honey.config.dsl.Rights
import honey.config.dsl.Rights.omit
import honey.config.dsl.ScriptOption.jvmOpts
import honey.config.dsl.ScriptOption.memory
import honey.config.example.HiveConfig
import honey.pack.JavaVersion
import java.util.*
import honey.config.example.HiveCellConfig as cell
import honey.config.example.HiveConfig as config

val appName = "honey-mouth"
val appVersion = "0.1.0-SNAPSHOT"

object Config {
  val staging = config("conf1",
    listOf(
      cell("", "ip4", "publicIp1", emptyList()),
      cell("", "ip3", "ip2", emptyList())
    )
  )

  val dev = config("dev",
    listOf(
      cell("", "192.168.31.182", "publicIp1", emptyList())
    )
  )

  val prod = staging.copy(name = "prod")
}

// Gradle will update placeholders
// In this script
// And pack it into the installation


//object : ReleaseDSL {
//  override fun <C : AppConfig> build(environment: String?): InstallDSLBuilder<C> {


val lambda = { environment: String ->

  build<HiveConfig>(environment) {
    config {
      StoredConfig(
        appName = appName,
        version = appVersion,
        revision = "8d89df",
        buildTime = Date(1511462850634L),
        team = "Andrey Chaschev",
        configs = listOf(Config.dev, Config.staging, Config.prod)
      )
    }

    require {
      "java" version "^9.0" via JavaVersion.parser
//    "badger-vcs" version "^0.0.3"
    }

    before {

    }

    users {
      "honey" inGroup "badger"
      "honey" inGroup "honey"
    }

    rights {
      default = omit

      add(Rights.readOnly)
      add(Rights.UserRights("normal", "a=rw", users("honey")))
    }

    folders {
//    app = Folder("/var/lib/$appName",
//      rights("normal")
//    )
//    "some" to Folder("/var/lib/$appName")
      app = Folder("build/test-install")
      "resources" to Folder("${app.path}/resources")
    }

    app {
      resources {
        "/honey/*.txt" into folders("resources")
        "/honey/badger/*.txt" into folders("resources")
      }
    }

    inFolder(folders().bin) {
      updateScript()

      script {
        id = "my-ass"
        appClass = "honey.MyAss"
        env = mapOf(
          "env" to "staging",
          "debug" to "info"
        )
        args = ""
        options = setOf(
          jvmOpts("-server"),
          memory(max = 500.MB())
        )
      }

    }

    inFolder("/usr/local/bin") {
      link(id = "my-ass")
      linkAllScripts()
    }

    after {
      println(users("honey"))
      println(folders().app)
    }

    updateApp {
      // will update/store app configuration in DB
      // will make schema migrations, to match new app logic
      installOptions

      //Active config is determined through the list of hosts
      config.getActiveConfig()
    }
  }
}

println("lambda = ${lambda}")
lambda

