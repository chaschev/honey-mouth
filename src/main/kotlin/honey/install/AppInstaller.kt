package honey.install

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import honey.config.AppConfig
import honey.config.dsl.InstallDSLBuilder
import honey.config.dsl.UpdateScriptDSLBuilder
import honey.config.example.HiveConfig
import honey.pack.Version
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
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


class AppInstaller<T : AppConfig>(
  val configClass: Class<T>,
  val dsl: InstallDSLBuilder<T>,
  internal val installOptions: HoneyMouthOptions<T>
) {

  init {
    installOptions.installer = this
  }

  fun run(args: Array<String>): Int {
    val parser = ArgParser(args, helpFormatter = helpFormatter)

    return HoneyMouthArgs(parser, configClass).run()
  }

  /**
   * At this stage we have all required JARs in our classpath.
   *
   *   mini-repo is a library cache. Library cache is useful for switching between versions.
   *   libs is an active version
   *
   * Run the release script.
   */
  fun install(options: HoneyMouthOptions<T>): Int {
    dsl.requiredVersions?.verify()

    dsl.before?.invoke()

    dsl.folders().makeDefault()

    runBlocking {
      val libDir = File(options.installationPath, "lib")

      // move all dependencies into the lib dir

      val libs = libDir.listFiles()

      println("moving ${libs?.size ?: 0} libs..")

      require(libs?.isNotEmpty() == true, { "$libDir/ folder must not be empty! Make sure you ran installer before or use --update-libs flat" })

      dsl.folders.lib.file.listFiles().forEach { it.delete() }

      if (libDir.canonicalPath != dsl.folders.lib.file.canonicalPath) {
        for (file in libs!!) {
          Files.move(file.toPath(), File(dsl.folders.lib.path, file.name).toPath())
        }
      } else {
        println("lib dir didn't move")
      }

      dsl.app?.extractResources()

      dsl.scripts.forEach { item ->
        item.writeScript()
      }
    }

    dsl.inFolders.forEach { inFolder ->
      inFolder.linkScripts()
    }

    dsl.after?.invoke()

    dsl.updateApp?.invoke()

    return 0
  }


  fun getInstalledVersion(): Version? {
    val updateScript = dsl.scripts.firstOrNull { it is UpdateScriptDSLBuilder } ?: return null

    return runBlocking {
      Version.parse(
        "${updateScript.id} --version".exec(2000).toString()
      )
    }
  }

  fun getActiveConfig(): T = dsl.config.getActiveConfig()

  companion object {
    val scriptEngineManager = ScriptEngineManager()
    val kotlinEngine = scriptEngineManager.getEngineByExtension("kts")!!

    private val dslMap = ConcurrentHashMap<Class<*>, InstallDSLBuilder<*>>()

    val helpFormatter = DefaultHelpFormatter("prologue", "epilogue")

    @JvmStatic
    fun main(args: Array<String>) {
      val options = HoneyMouthOptions<HiveConfig>(InstallMode.update, false)

      val r = AppInstaller(
        configClass = HiveConfig::class.java,
        dsl = AppInstaller.dsl(HiveConfig::class.java, options, "dev"),
        installOptions = options
      ).run(args)

      System.exit(r)
    }

    fun <T : AppConfig> dsl(
      aClass: Class<T>,
      options: HoneyMouthOptions<T> = HoneyMouthOptions(),
      environment: String = "auto"): InstallDSLBuilder<T> {

      return dslMap.getOrPut(aClass, {
        val installScript = StupidJavaResources.readResource(this::class.java, "/install.kts")

        println("wait a sec. evaluating install script, it is a little slow today...")

        val dslLambda = kotlinEngine.eval(installScript) as ((env:String) -> InstallDSLBuilder<T>)

        val dsl = dslLambda(environment)

        println("using config: " + dsl.config)

        dsl.useOptions(options)

        dsl
      }) as InstallDSLBuilder<T>
    }
  }
}
