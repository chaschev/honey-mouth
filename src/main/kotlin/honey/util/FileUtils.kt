package honey.util

import java.io.File

object FileUtils {
    fun write(path: String, s: String)  = File(path).writeText(s)

    fun read(path: String): String = File(path).readText()
    fun mkdirs(path: String): Boolean = File(path).mkdirs()
    fun exists(path: String): Boolean = File(path).exists()

    fun readAppResource(path: String, obj: Any): String? {
        val stream = obj.javaClass.getResourceAsStream(path) ?: return null

        return stream
            .bufferedReader()
            .use { it.readText() }
    }
}