package honey.install

import kotlinx.coroutines.experimental.delay
import java.io.InputStream

class ProcessWatch(val process: Process) {
  suspend fun await(timeoutMs: Int = Int.MAX_VALUE): StringBuilder {
    val startedAtMs = System.currentTimeMillis()

    val stderr = process.errorStream
    val stdout = process.inputStream

    val out = StringBuilder(128)

    while (process.isAlive && System.currentTimeMillis() - startedAtMs < timeoutMs) {
      copyStream(stdout, out)
      copyStream(stderr, out)

      delay(10)
    }

    copyStream(stdout, out)
    copyStream(stderr, out)

    return out
  }

  private fun copyStream(stream: InputStream, out: StringBuilder) {
    val available = stream.available()
    if (available > 0) {
      val bytes = ByteArray(available)
      stream.read(bytes)
      out.append(String(bytes))
    }
  }
}

fun Process.watch() = ProcessWatch(this)

suspend fun String.exec(timeoutMs: Int = 5000, inheritIO: Boolean = false): StringBuilder {
  // todo = a better split
  val b = ProcessBuilder().command(this.split("\\s".toRegex()))
  if(inheritIO) b.inheritIO()
  return b.start().watch().await(timeoutMs)
}