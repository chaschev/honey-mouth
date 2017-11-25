package honey.config.dsl

import honey.config.AppConfig
import honey.install.StupidJavaResources
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import java.io.File
import java.util.zip.ZipFile

class AppDSLBuilder(val devJarPath: String?) {
  val resourcesList = ArrayList<Pair<Regex, Folder>>()


  fun resources(builder: ResourcesDSLBuilder.() -> Unit): ResourcesDSLBuilder {
    return ResourcesDSLBuilder().apply(builder)
  }

  inner class ResourcesDSLBuilder {
    infix fun String.into(folder: Folder) {
      resourcesList.add(Pair(this.replace("*", ".*").toRegex(), folder))
    }
  }

  fun extractResources() {
    if (resourcesList.size > 0) {
      val zipFile = ZipFile(StupidJavaResources.getMyJar(javaClass, devJarPath))

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

