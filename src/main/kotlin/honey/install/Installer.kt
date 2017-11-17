package honey.install

import honey.maven.DumbMavenRepo
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels


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


class Installer {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Installer().install()
    }
  }

  fun install() {
    val libDir = File("lib")

    libDir.mkdir()

    println("downloading runtime libraries...")

//    downloadJavaWay("http://central.maven.org/maven2/jline/jline/2.14.5/jline-2.14.5.jar", File(""))

    MavenArtifactResolver(listOf(
      DumbMavenRepo("http://dl.bintray.com/kotlin/kotlin-eap-1.2"),
      DumbMavenRepo("http://central.maven.org/maven2")
    )).resolve(libDir,
      "org.jetbrains.kotlin:kotlin-stdlib:1.2.0-rc-39",
      "jline:jline:2.14.5"
    )
  }


}