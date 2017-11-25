package honey.install

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import honey.config.AppConfig
import honey.config.dsl.UpdateScriptDSLBuilder
import java.io.File

enum class InstallMode {
  install, update, noInstall
}

data class HoneyMouthOptions<T : AppConfig>(
  val installMode: InstallMode = InstallMode.noInstall,
  val updateJars: Boolean = false,
  val installationPath: String = ".",
  internal val javaInstaller: Installer? = null,
  val devJarPath: String? = null
) {
  lateinit var installer: AppInstaller<T>

  val oldVersion by lazy { installer.getInstalledVersion() }
  val newVersion by lazy { javaInstaller!!.version }
}

internal class HoneyMouthArgs<T : AppConfig>(val parser: ArgParser, val resourcesClass: Class<T>) {

  val force by parser.flagging("--force",
    help = "force download JARs").default(false)

  val updateLibs by parser.flagging("--update-libs", help = "force download JARs")
    .default(false)

  val installVersion by parser.storing("--install-version", help = "Specify the version to install. Additional values: latest, jar")
    .default("jar")

  val environment by parser.storing("--environment", help = "Explicitly specify the environment. Normally, it should be loaded from hosts").default("auto")

  val artifact by parser.storing("--artifact", help = "Explicitly specify the installed/listed artifact group:module")
    .default("auto")

  val repository by parser.storing("--repository", help = "Explicitly specify the repository for installed artifact")
    .default("auto")

  val installService by parser.flagging("--install-service", help = "Install service (Systemd so far)")

  val listVersions by parser.flagging("--list-versions", help = "list available version")

  val version by parser.flagging("--version", help = "Get version of this software")

  val mode by parser.mapping(
    "--update" to InstallMode.update,
    "--install" to InstallMode.install,
    help = "mode of operation").default(InstallMode.install)


  fun run(): Int {
    // load dsl
    // update-${appName}
    //   should return current version VIA loading default config
    //
    // ok DslAPI.getInstallVersion via update-${appName}
    // if DslAPI.isInstalled, error "already installed, use --update"
    //   else
    // Installer.Install
    //
    // if DslAPI.isInstalled && --update
    //  DslAPI.check via DslAPI.getInstalledVersion
    //  if(versionOk && !force) exit("already latest version use --force to force update"
    //
    // installMode = ... ［from command line]
    // environment = ...  [from command line or from DSL, i.e. check host ip]

    if (listVersions) {
      val (art, repo) = getArtifactAndRepo()

      val meta = Installer().getMetadata(art, repo)

//      println("found ${meta.versions.size} versions: ")
      println(meta.versions.joinToString("\n") {" $it"})

      return 0
    }

    val javaInstaller = Installer()

    if (version) {
      println(javaInstaller.version)
      return 0
    }

    val options = HoneyMouthOptions<T>(
      mode,
      updateLibs || (force && mode == InstallMode.update),
      System.getenv(UpdateScriptDSLBuilder.INSTALLATION_PATH) ?: ".",
      javaInstaller
    )

    val installer = AppInstaller(resourcesClass, AppInstaller.dsl(resourcesClass, options, environment),  options)

    // here, newVersion requires to determine our jar

    if (mode == InstallMode.update) {
      if (options.oldVersion == null) {
        println("couldn't find previously installed version")
        return 1
      }
    }

    if (options.installMode == InstallMode.update) {
      val (art, repo) = getArtifactAndRepo()

      val downloadVersion =
        when (installVersion) {
          "jar" -> null
          "latest" -> Installer().getMetadata(art, repo).release
          else -> installVersion
        }

      val downloadArt = art.substringBeforeLast(':') + ":" + downloadVersion

      println("force updating jars...")

      if(installVersion == "jar"){
        javaInstaller
          .setMyJar(StupidJavaResources.getMyJar(resourcesClass, Installer.MY_JAR))
          .setMiniRepoDir(File(options.installationPath))
          .resolveAll(options.updateJars)

      }else {
        javaInstaller
          .setMiniRepoDir(File(options.installationPath))
          .downloadAndInstall(downloadArt, repo, options.updateJars)
      }

      return 0
    }

    return installer.install(options)

//    if (initialInstall) {
//      if(mode == InstallMode.update) throw IllegalStateException("how come?")
//
//
//
//      if(ini)
//    }

/*
  add bin/update-${appName} to honey-mouth --version=latest
  add --install-service

  download new jar
   run honey.Installer --update (get the latest repo version, force check sha1 for all non-central jars)
   run new updater:
    wait for old jvm to finish OR just replace all
    replace jar
    run new jar
     check the version and follow with full dsl installation
*/
  }

  private fun getArtifactAndRepo(): Pair<String, String> {
    val art: String
    val repo: String

    val moduleDependencies = ModuleDependencies(StupidJavaResources.getMyJar(resourcesClass, Installer.MY_JAR))

    art = if (artifact == "auto") {
      moduleDependencies.me.substringBeforeLast(':')
    } else {
      artifact
    }

    repo = if (repository == "auto") {
      moduleDependencies.myRepo.root()
    } else {
      repository
    }

    return Pair(art, repo)
  }
}

