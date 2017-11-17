package honey.util

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * @return Non-null if the folder now exists
 */
fun FileUtils.mkdirsSafely(path:String): String? {
    return if(!FileUtils.exists(path)){
        if(!FileUtils.mkdirs(path)){
            if(FileUtils.exists(path)) {
                path
            } else {
                null
            }
        } else{
            path
        }
    }else {
        path
    }
}

fun FileUtils.mkdirsOrThrow(path: String): String {
    if(mkdirsSafely(path) == null) throw Exception("could not create dir: $path")

    return path
}

fun FileUtils.download(url: String, out: File): File {
        URL(url).openStream().use({
            Files.copy(it, out.toPath(), StandardCopyOption.REPLACE_EXISTING)
        })

    return out
}

