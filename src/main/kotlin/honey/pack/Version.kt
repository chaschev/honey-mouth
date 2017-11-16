package honey.pack

data class SimpleVersion(override val numbers: List<Comparable<Any>>) : Version {
    override fun toString(): String =
        "Version(${asString()})"

    override fun equals(other: Any?): Boolean = super.equalsImpl(other)
}

interface Version : Comparable<Version> {
    companion object {
        private val REGEX = "[._\\-]".toRegex()

        val ZERO = parse("0")
        val MAX = parse("9999")

        fun parse(s: String): Version =
          SimpleVersion(s.split(REGEX) as List<Comparable<Any>>)
//          .map {
//              (it.toIntOrNull() ?: it) as Comparable<Any>
//          })
    }


    val numbers: List<Comparable<Any>>

    fun isHigherThan(other: Version): Boolean =
        compareTo(other) >= 0

    operator fun get(i: Int): Int = numbers[i] as Int

    override fun compareTo(other: Version): Int {
        numbers.forEachIndexed { i, number1 ->
            if(other.numbers.size == i) return 1

            val number2 = other.numbers[i]

            if(number1.javaClass != number2.javaClass)
                throw IllegalArgumentException("We are trying to compare horses with courses: $number1 and $number2")

            val r = number1.compareTo(number2)

            if(r != 0) return r
        }

        if(numbers.size < other.numbers.size) return -1

        return 0
    }

    fun asString() = numbers.joinToString(".")

    fun equalsImpl(other: Any?): Boolean {
        if(other == null) return false
        if(other is Version) return compareTo(other) == 0

        return false
    }
}

object VersionRange {
    fun parse(s: String): ClosedRange<Version> {
        if(s.startsWith("^")) {
            return Version.parse(s.substring(1)) .. Version.MAX
        }

        if(s.startsWith("[")){
            val rangeString = s.split(",")
            val r0 = rangeString[0].substring(1)
            val r1 = rangeString[1].substring(0, rangeString[1].lastIndex)

            return if(r1.isEmpty()) {
                Version.parse(r0)..Version.MAX
            }else {
                Version.parse(r0)..Version.parse(r1)
            }
        }

        val x = Version.parse(s)

        return x..x
    }
}