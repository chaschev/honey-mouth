import honey.config.MB
import honey.config.StoredConfig
import honey.config.dsl.Folder
import honey.config.dsl.InstallDSLBuilder.Companion.build
import honey.config.dsl.Rights
import honey.config.dsl.Rights.omit
import honey.config.dsl.ScriptOption.jvmOpts
import honey.config.dsl.ScriptOption.memory
import honey.config.example.ExampleConfig
import honey.config.example.HiveConfigs
import java.util.*

val appName = "honey-badger"

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
      version = "0.0.1",
      revision = "6efe8a",
      buildTime = Date(),
      team = "Andrey Chaschev",
      configs = listOf(ExampleConfig.dev, ExampleConfig.staging, ExampleConfig.prod)
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
