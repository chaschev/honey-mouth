import honey.config.MB
import honey.config.StoredConfig
import honey.config.dsl.Folder
import honey.config.dsl.InstallDSLBuilder.Companion.build
import honey.config.dsl.Rights
import honey.config.dsl.Rights.omit
import honey.config.dsl.ScriptOption.jvmOpts
import honey.config.dsl.ScriptOption.memory
import honey.config.example.HiveConfigs

import java.util.Date

import honey.config.example.HiveConfigs as configs
import honey.config.example.HiveConfig as config
import honey.config.example.HiveCellConfig as cell


val appName = "honey-mouth"
val appVersion = "0.0.1-SNAPSHOT"

object Config {
  val staging = configs(
    "staging",
    config("conf1",
      listOf(
        cell("", "ip4", "publicIp1", emptyList()),
        cell("", "ip3", "ip2", emptyList())
      )
    ),
    config("conf2",
      listOf(
        cell("", "ip4", "publicIp2", emptyList()),
        cell("", "ip3", "ip2", emptyList())
      )
    )
  )

  val dev = staging.copy(name = "dev")
  val prod = staging.copy(name = "prod")
}

// Gradle will update placeholders
// In this script
// And pack it into the installation

build<HiveConfigs> {
  require {
    "java" version "^9.0"
    "javac" version "^9.0"
    "badger-vcs" version "^0.0.3"
  }

  before {

  }

  config {
    StoredConfig(
      appName = appName,
      version = appVersion,
      revision = "945b93",
      buildTime = Date(1511184575962L),
      team = "Andrey Chaschev",
      configs = listOf(Config.dev, Config.staging, Config.prod)
    )
  }

  users {
    "honey" inGroup "badger"
    "honey" inGroup "honey"
  }

  rights {
    default = omit

    add(Rights.UserRights.readOnly)
    add(Rights.UserRights("normal", "a=rw"))
  }

  folders {
    app = Folder("/var/lib/$appName",
      users("honey"),
      rights("normal")
    )
    "some" to Folder("/var/lib/$appName")
    "resources" to Folder("$app/resources")
  }

  app {
    resources {
      "/honey/*" into folders("resources")
      "/honey/badger/*" into folders("resources")
    }
  }

  inFolder(folders().bin) {
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

  inFolder("/usr/bin") {
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
  }
}


