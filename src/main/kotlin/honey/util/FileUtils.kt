package honey.util

import java.io.File

object FileUtils {
  fun write(path: String, s: String) = File(path).writeText(s)

  fun read(path: String): String = File(path).readText()
  fun mkdirs(path: String): Boolean = File(path).mkdirs()
  fun exists(path: String): Boolean = File(path).exists()

}

fun FileUtils.readAppResource(path: String, resourceObj: Any): String? {
  val stream = resourceObj.javaClass.getResourceAsStream(path) ?: return null

  return stream
    .bufferedReader()
    .use { it.readText() }
}

fun FileUtils.extractResource(path: String, dir: File, resourceObj: Any): Long? {
  val stream = resourceObj.javaClass.getResourceAsStream(path) ?: return null

  val filename = File(path).name

  return stream
    .use { input ->
      File(dir, filename).outputStream().use { output ->
        input.copyTo(output)
      }
    }
}
