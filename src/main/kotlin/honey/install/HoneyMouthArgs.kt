package honey.install

import com.xenomachina.argparser.ArgParser

enum class InstallMode {
  install, update
}

internal class HoneyMouthArgs(val parser: ArgParser) {
  val force by parser.flagging("--force",
    help = "force download JARs")

  val installVersion by parser.storing("--install-version", "Specify the version to install")

  val environment by parser.storing("--environment", "Explicitly specify the environment. Normally, it should be loaded from hosts")

  val installService by parser.flagging("--install-service", help ="Install service (Systemd so far)")

  val update by parser.flagging("--update", help = "update installed app")

  val listVersions by parser.flagging("--list-versions")

  fun work() {
    // load dsl
    // if DslAPI.isInstalled, error "already installed, use --update"
    //   else
    // Installer.Install
    //
    // if DslAPI.isInstalled && --update
    //  DslAPI.check via getInstalledVersion
    //  if(versionOk && !force) exit("already latest version use --force to force update"
    //
    // installMode = ... ［from command line]
    // environment = ...  [from command line or from DSL, i.e. check host ip]

    if(listVersions) {

    }

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

}
