package honey.install

import honey.config.MB
import honey.config.dsl.*
import honey.config.example.HiveConfig
import honey.pack.JavaVersion


class DefaultInstallDef(
  override val devJar: String? = null
) : ReleaseDSLDef<HiveConfig>(HiveConfig::class.java, devJar) {
  override fun build(environment: String): InstallDSLBuilder<HiveConfig> {
    return InstallDSLBuilder.build(environment) {
      config {
        buildProps.toStoredConfig(
          team = "Andrey Chaschev",
          configs = *arrayOf(Config.dev, Config.staging, Config.prod)
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
        default = Rights.omit

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
        updateScript(javaClass.name)

        script {
          id = "my-ass"
          appClass = "honey.MyAss"
          env = mapOf(
            "env" to "staging",
            "debug" to "info"
          )
          args = ""
          options = setOf(
            ScriptOption.jvmOpts("-server"),
            ScriptOption.memory(max = 500.MB())
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
        getActiveConfig()
      }
    }
  }
}