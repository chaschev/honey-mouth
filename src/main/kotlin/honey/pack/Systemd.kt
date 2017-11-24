package honey.pack

import honey.install.exec
import kotlinx.coroutines.experimental.runBlocking

object Systemd {
  fun start(service: String, timeoutMs: Int = 600 * 1000)=
    callCommand(service, "start", timeoutMs)

  fun restart(service: String, timeoutMs: Int = 600 * 1000)=
    callCommand(service, "restart", timeoutMs)

  fun stop(service: String, timeoutMs: Int = 600 * 1000)=
    callCommand(service, "stop", timeoutMs)

  fun status(service: String, timeoutMs: Int = 600 * 1000)=
    callCommand(service + " --no-pager", "status", timeoutMs, true)

  internal fun callCommand(service: String, command: String, timeoutMs: Int, inheritIO: Boolean = true) {
    runBlocking {
      "systemctl $command $service".exec(timeoutMs, inheritIO)
    }
  }
}