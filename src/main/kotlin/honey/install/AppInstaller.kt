package honey.install

import honey.config.dsl.InstallDSLBuilder
import honey.util.FileUtils
import honey.util.extractResource
import honey.util.mkdirsSafely
import honey.util.readAppResource
import java.io.File
import javax.script.ScriptEngineManager


/*

User extends Installer Class

java -jar my-ass.jar install --env staging

KGit application
 curl to fetch minimal runtime?
 java -jar my-ass.jar install
   Checks if There is Installation Runtime in classpath
    Downloads Runtime to lib/. Kotlin libs, what else?
    Restarts with Updated Jars
     java -jar my-ass.jar install --have-runtime
     System.Exit

 Installation (must be Kotlin + a couple of libs):
  "Simple DSL for JARs (mem spec, etc) + Installation Script". Take from AppAssempler https://goo.gl/Cy1Qp5. That really should not be much
  Installation Script:
    Creates Dirs (required: lib dir - in DSL)    !! Classpath is easy https://stackoverflow.com/a/219801/1851024
    Copies Files from Resources
    Creates Running Scripts

  App Jar will hold information about runnable configurations in Kotlin DSL
  Installer will run this DSL and generate all required scripts

 KGit: Add Installation
  Prerequisite is only Java (No Gradle or Maven)
   with Curl: Download Installation Script in SH
   with Java
    Read Parameters from Environment and Command Line
    Download All Required Jars (read from package)
    Download Default Configuration
     Optionally: prompt questions for installation paths, versions, features, etc
    Create Shell Shortcuts (from Configuration)

  Later (big business): verify downloaded JARs - the source must be SSL-ed

Don't need a tool like npm, there is Gradle. Though Gradle doesn't provide API for tool development

*/


class AppInstaller {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      AppInstaller().install()
    }
  }


  val scriptEngineManager = ScriptEngineManager()
  val kotlinEngine = scriptEngineManager.getEngineByExtension("kts")!!

  /**
   * At this stage we have all required JARs in our classpath.
   *
   * Run the release script.
   */
  fun install() {
    FileUtils.mkdirsSafely("kt")

    FileUtils.extractResource("/Config.kt", File("kt"), this)
    FileUtils.extractResource("/install.kts", File("kt"), this)


    val dsl = kotlinEngine.eval(File("kt/install.kts").reader()) as InstallDSLBuilder<*>

    println(dsl.config)
  }
}


//    engineManager.engineFactories.forEach {
//      println(it.engineName + " " + it.names)
//    }