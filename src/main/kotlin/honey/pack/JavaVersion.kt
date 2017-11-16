package honey.pack

data class JavaVersion(
  val isOpenJDK: Boolean,
  val version: Version
) : Version {
  override val numbers: List<Comparable<Any>> = version.numbers

  fun major(): String = version.numbers[0] as String
  fun update(): String = version.numbers[1] as String

  fun isHigherThan(other: JavaVersion): Boolean =
    compareTo(other) >= 0

  override fun toString(): String =
    "${if (isOpenJDK) "openjdk" else "oraclejdk"} version ${version.asString()}"

  companion object {
    fun parseJavaVersion(s: String): JavaVersion {
      val isOpenJDK = s.contains("openjdk")

      val oldFormat1 = "version\\s\"1.(\\d+).\\d+_(\\d+)\"".toRegex().tryFind(s)
      val newFormat1 = "version\\s\"(\\d+)\\.\\d+\\.(\\d+)\"".toRegex().tryFind(s)

      val version =
        if (oldFormat1 != null) {
          listOf(oldFormat1[1], oldFormat1[2])
        } else if (newFormat1 != null) {
          listOf(newFormat1[1], newFormat1[2])
        } else {
          val major = "version \"(\\d+)".toRegex().tryFind(s)!![1]
          val build = "build (\\d+)".toRegex().tryFind(s)!![1]
          listOf(major, build)
        }

      return JavaVersion(isOpenJDK, SimpleVersion(version as List<Comparable<Any>>))
    }

  }

  override fun equals(other: Any?): Boolean = super.equalsImpl(other)
}




fun Regex.tryFind(s: String): List<String>? =
  find(s)?.groups?.map { it?.value ?: "null" }