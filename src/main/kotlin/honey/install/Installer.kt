package honey.install

import honey.maven.DumbMavenRepo
import honey.maven.MavenRepo
import honey.util.FileUtils
import honey.util.download
import honey.util.mkdirsSafely
import mu.KLogging
import rocket.util.HashUtils
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

  fun downloadJavaWay(url: String, dest: File) {
    println("downloading from $url to $dest")

    val website = URL(url)
    val rbc = Channels.newChannel(website.openStream())
    val fos = FileOutputStream(dest)

    fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)
  }

  class MavenArtifactResolver(
    val repos: List<MavenRepo>
  ) {

    fun resolve(cacheFolder: File, vararg arts: String) {
      for(art in arts) {
        resolve(art, cacheFolder) ?: throw Exception("couldn't resolve $art")
      }
    }

    fun resolve(art: String, cacheFolder: File): Pair<File, File>? {
      val (group, module, version) = art.split(':')

      for(repo in repos) {
        val (url, file) = repo.resolveUrl(group, module, version)

        val jarFile = File(cacheFolder, "$file.jar")
        val sha1File = File(cacheFolder, "$file.jar.sha1")

        val result = Pair(jarFile, sha1File)

        val cached = isCached(sha1File, jarFile, file, result)

        if(cached != null) return cached

        val sha1 = try {
          FileUtils.download("$url.jar.sha1", sha1File).readText().trim().substringBefore(" ")
        } catch (e: Exception) {
          null
        }

        if (sha1 == null) {
          sha1File.delete()
        } else {
          print("GET $url.jar... ")
          try {
            FileUtils.download("$url.jar", jarFile)

            val actualSha1 = HashUtils.sha1(jarFile)

            if (sha1 != actualSha1) {
              println("downloaded a file, and sha1 didn't match: $actualSha1 (actual) vs $sha1 (expected)")
              return null
            }
          } catch (e: Exception) {
            throw Exception("can't download url $url.jar for artifact $art")
          }

          println("ok")

          return Pair(jarFile, sha1File)
        }
      }

      return null
    }

    private fun isCached(sha1File: File, jarFile: File, file: String, result: Pair<File, File>): Pair<File, File>? {
      val cached = if (sha1File.exists() && jarFile.exists()) {
        val sha1 = sha1File.readText().trim().substringBefore(" ")
        val actualSha1 = HashUtils.sha1(jarFile)

        if (sha1 != actualSha1) {
          println("files were downloaded, but sha1 didn't match: $actualSha1 (actual) vs $sha1 (expected)")
          null
        } else {
          println("cached: $file")

          result
        }
      } else {
        null
      }
      return cached
    }
  }

  private fun updateAllJars() {
    /*logger.info("reading jars resource file...")
    val jarsFile = FileUtils.readAppResource("/jars.txt", this)
      ?: throw Exception("not found: /jars.txt")

    FileUtils.mkdirsSafely("lib")

    val repos = listOf(
      DumbMavenRepo("http://central.maven.org/maven2"),
      *repos.split(",").map { DumbMavenRepo(it) }.toTypedArray()
    )

    for (line in jarsFile.trim().split("\n")) {
      val g = line.split(":")

      val group = g[0].split(".").joinToString("/")
      val module = g[1]
      val version = g[2]

      val repo = repos.find { repo ->

        val (url, file) = repo.resolveUrl(group, module, version)

        val sha1File = File("lib/$file.jar.sha1")
        val jarFile = File("lib/$file.jar")

        if (sha1File.exists() && jarFile.exists()) {
          val sha1 = sha1File.readText().trim().substringBefore(" ")
          val actualSha1 = HashUtils.sha1(jarFile)

          if (sha1 != actualSha1) {
            println("files were downloaded, but sha1 didn't match: $actualSha1 (actual) vs $sha1 (expected)")
          } else {
            println("cached: $file")
            return@find true
          }
        }

        val sha1 = try {
          FileUtils.download("$url.jar.sha1", sha1File).readText().trim().substringBefore(" ")
        } catch (e: Exception) {
          null
        }

        if (sha1 == null) {
          false
        } else {
          print("GET $url.jar... ")
          try {
            FileUtils.download("$url.jar", jarFile)

            val actualSha1 = HashUtils.sha1(jarFile)

            if (sha1 != actualSha1) {
              println("downloaded a file, and sha1 didn't match: $actualSha1 (actual) vs $sha1 (expected)")
              return@find false
            }
          } catch (e: Exception) {
            throw Exception("can't download url $url.jar for artifact $g")
          }

          println("ok")

          true
        }
      }

      if (repo == null) {
        println("\nERROR: didn't find repo for $line")
      }

    }*/

  }
}