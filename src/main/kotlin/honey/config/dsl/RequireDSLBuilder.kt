package honey.config.dsl

import honey.install.ProcessWatch
import honey.install.exec
import honey.pack.Version
import honey.pack.VersionParser
import honey.pack.VersionRange
import kotlinx.coroutines.experimental.runBlocking

class RequireDSLBuilder {
  val list = ArrayList<Pair<String, String>>()
  val viaList = ArrayList<Triple<String, String, VersionParser>>()

  infix fun String.version(versionTemplate: String): Pair<String, String> {
    val pair = Pair(this, versionTemplate)
    list.add(pair)

    return pair
  }

  infix fun Pair<String, String>.via(parser: VersionParser) {
    list.removeAt(list.lastIndex)
    viaList.add(Triple(first, second, parser))

  }

  fun verify() {
    println("checking system requirements")

    list.forEach { (command, rangeS) ->
      val versionS = runBlocking {
        ProcessWatch(ProcessBuilder(command, "--version").inheritIO().start()).await(1000)
      }.toString()

      val parsedVersion = Version.parse(versionS)

      val range = VersionRange.parse(rangeS)

      if (!range.contains(parsedVersion)) {
        throw Exception("version didn't match for $command: found $parsedVersion, expected: $range")
      }

      println("[ok] found $command version $parsedVersion")
    }

    runBlocking {
      viaList.forEach { (command, rangeS, parser) ->
        val versionS = "$command ${parser.versionParam}".exec().toString()

        val parsedVersion = parser.parse(versionS)

        val range = VersionRange.parse(rangeS)

        if (!range.contains(parsedVersion)) {
          throw Exception("version didn't match for $command: found $parsedVersion, expected: $range")
        }

        println("[ok] found $command version $parsedVersion")
      }
    }
  }
}