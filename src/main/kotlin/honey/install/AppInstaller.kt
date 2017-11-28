package honey.install

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import honey.config.AppConfig
import honey.config.dsl.InstallDSLBuilder
import honey.config.dsl.ReleaseDSLDef
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


open class AppInstaller<T : AppConfig>(
  open val dsl: InstallDSLBuilder<T>,
  open val options: HoneyMouthOptions<T>
) {

  init {
    println("app installOptions = ${options}")
    options.installer = this

  }

  /**
   * At this stage we have all required JARs in our classpath.
   *
   *   mini-repo is a library cache. Library cache is useful for switching between versions.
   *   libs is an active version
   *
   * Run the release script.
   */
  fun install(): Int {
    println("app installOptions = ${options}")

    dsl.requiredVersions?.verify()

    dsl.before?.invoke()

    dsl.folders().makeDefault()

    runBlocking {
      val libDir = File(options.installationPath, "lib")

      // create/update temp lib dir which is left after Installer (java)
      // these libs are also our runtime
      Installer()
        .setMyJar(options.myJar)
        .resolveDepsToLibFolder(libDir)

      // move all dependencies into the lib dir
      val libs = libDir.listFiles()

      println("moving ${libs?.size ?: 0} libs to ${dsl.folders.lib.path}")

      require(libs?.isNotEmpty() == true, { "$libDir/ folder must not be empty! Make sure you ran installer before or use --update-libs flat" })

      //empty target dir to avoid version conflicts after updating to new versions
      //FUCK JAVA BUG
      // files are deleted if you run this line, the stay if you don't run it
      // IDEA interference? FUCK HERR
      dsl.folders.lib.file.listFiles().forEach { it.delete() }
      dsl.folders.lib.file.delete()

      if (libDir.canonicalPath != dsl.folders.lib.file.canonicalPath) {
        Files.move(libDir.toPath(), dsl.folders.lib.file.toPath())
//        for (file in libs!!) {
//          //fuck java move method won't work
//
//          Runtime.getRuntime().exec("mv ${file.toPath()} ")
////          Files.move(file.toPath(), File(dsl.folders.lib.path, file.name).toPath())
//        }
      } else {
        println("lib dir no need move")
      }

      dsl.app?.extractResources()

      dsl.scripts.forEach { item ->
        item.writeScript()
//        println("starting service...")
//        Systemd.start(id)
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
      val versionOutput = "${updateScript.id} --version".exec(2000).toString()

      Version.parse(
        versionOutput.substringAfter(' ').substringBefore(' ')
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
      val parser = ArgParser(args, helpFormatter = helpFormatter)

      val r = HoneyMouthArgs(parser, HiveConfig::class.java, "build/libs/honey-mouth-0.1.1-SNAPSHOT.jar").run()

      System.exit(r)
    }

    fun <T : AppConfig> dsl(
      releaseDSLDef: ReleaseDSLDef<T>,
      options: HoneyMouthOptions<T>,
      environment: String = "auto"): InstallDSLBuilder<T> {

      return dslMap.getOrPut(releaseDSLDef.javaClass, {
        releaseDSLDef.build(environment).useOptions(options).build()
       /* val installScript = StupidJavaResources.readResource(this::class.java, "/install.kts")

        println("wait a sec. evaluating install script, it is a little slow today...")

        val dslLambda = kotlinEngine.eval(installScript) as ((env:String) -> InstallDSLBuilder<T>)

        val dsl = dslLambda(environment)

        println("using config: " + dsl.config)

        dsl.useOptions(options)

        dsl*/
      }) as InstallDSLBuilder<T>
    }
  }
}
