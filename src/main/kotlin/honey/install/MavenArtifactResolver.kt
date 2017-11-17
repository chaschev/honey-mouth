package honey.install

import honey.maven.MavenRepo
import honey.util.FileUtils
import honey.util.download
import rocket.util.HashUtils
import java.io.File

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
      val file = repo.file(module, version)
      val url = repo.resolveUrl(group, module, version)

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