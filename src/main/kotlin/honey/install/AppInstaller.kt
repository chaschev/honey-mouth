package honey.install

import honey.config.dsl.InstallDSLBuilder
import honey.util.FileUtils
import honey.util.extractResource
import honey.util.mkdirsSafely
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import java.io.File
import java.util.zip.ZipFile
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
  ok "Simple DSL for JARs (mem spec, etc) + Installation Script". Take from AppAssempler https://goo.gl/Cy1Qp5. That really should not be much
  ok Installation Script:
    Creates Dirs (required: lib dir - in DSL)    !! Classpath is easy https://stackoverflow.com/a/219801/1851024
    Copies Files from Resources
    Creates Running Scripts

  Release Plugin:
    task write /jars
    task update appName, appVersion, revision



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

    FileUtils.extractResource("/install.kts", File("kt"), this)

    val dsl = File("kt/install.kts").reader().use { reader ->
      val dsl = kotlinEngine.eval(reader) as InstallDSLBuilder<*>
      println(dsl.config)
      dsl
    }

    dsl.requiredVersions?.verify()

    dsl.before?.invoke()

    dsl.folders().map.values.forEach { folder ->
      folder.file.mkdirsOrFail()

      runBlocking {
        folder.applyRights()
      }
    }

    dsl.app?.apply {
      if (resourcesList.size > 0) {
        val zipFile = ZipFile(Installer.getMyJar(javaClass, Installer.MY_JAR))

        zipFile.entries().asSequence().forEach { entry ->
          if (!entry.isDirectory) {

            resourcesList.forEach { (pattern, folder) ->
              if (pattern.containsMatchIn("/" + entry.name)) {
                zipFile.getInputStream(entry).use { input ->
                  val destFile = File(folder.file, entry.name)

                  destFile.absoluteFile.parentFile.mkdirsOrFail()

                  println("extracting /${entry.name} -> $destFile")
                  destFile.outputStream().use { output ->
                    input.copyTo(output)
                  }
                }
              }
            }
          }
        }
      }
    }

  }
}




//    engineManager.engineFactories.forEach {
//      println(it.engineName + " " + it.names)
//    }