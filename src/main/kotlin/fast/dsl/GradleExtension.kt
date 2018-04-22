package fast.dsl

import honey.config.dsl.UserRights

class GradleExtension(
): DeployFastExtension() {
  val zippedApp = ZippedAppExtension()

  companion object {
    fun dsl() = DeployFastDSL.deployFast(GradleExtension()) {
      info {
        name = "Gradle Extension"
        author = "Andrey Chaschev"
      }

      beforeTasks {
        init {
          ext.zippedApp.configure("gradle", "4.3.2", "TODO") {
            archiveName = "$name-$version.tar.gz"

            symlinks {
              "gradle" to "/bin/gradle.sh" with UserRights.omit
            }
          }
        }
      }


      tasks {
        task("install_gradle") {
          ext.zippedApp.tasks.install().run()
        }
      }

    }

  }
}


