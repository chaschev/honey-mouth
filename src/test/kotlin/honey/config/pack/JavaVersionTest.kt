package honey.config.pack

import com.nhaarman.mockito_kotlin.mock
import honey.pack.JavaVersion
import honey.pack.Version
import honey.pack.Version.Companion.MAX
import honey.pack.Version.Companion.parse
import honey.pack.VersionRange
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

class JavaVersionTest {
    companion object {
        val oracleJDK8 = """
java version "1.8.0_151"
Java(TM) SE Runtime Environment (build 1.8.0_151-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.151-b12, mixed mode)
""".trimIndent()

        val openJDKOutput8 = """
openjdk version "1.8.0_131"
OpenJDK Runtime HiveConfig (build 1.8.0_131-8u131-b11-2ubuntu1.16.04.3-b11)
OpenJDK 64-Bit Server VM (build 25.131-b11, mixed mode)
        """.trimIndent()

        val openJDKOutput9 = """
openjdk version "9-internal"
OpenJDK Runtime HiveConfig (build 9-internal+0-2016-04-14-195246.buildd.src)
OpenJDK 64-Bit Server VM (build 9-internal+0-2016-04-14-195246.buildd.src, mixed mode)
        """.trimIndent()

        val oracleOutput = """java version "9"
Java(TM) SE Runtime HiveConfig (build 9+181)
Java HotSpot(TM) 64-Bit Server VM (build 9+181, mixed mode)
        """.trimIndent()

        val oracleJDK9_2 = """java version "9.0.1"
Java(TM) SE Runtime Environment (build 9.0.1+11)
Java HotSpot(TM) 64-Bit Server VM (build 9.0.1+11, mixed mode)""".trimIndent()

        val oracleJava8 = JavaVersion(false, parse("8.0"))
        val oracleJava8_131 = JavaVersion(false, parse("8.131"))

    }
    @Test
    fun testParse() {
        assertEquals(
            JavaVersion(false, Version.parse("9.1")),
            parseJV(oracleJDK9_2)
        )

        assertEquals(
            JavaVersion(false, Version.parse("8.151")),
            parseJV(oracleJDK8)
        )

        assertEquals(
            JavaVersion(true, Version.parse("8.131")),
            parseJV(openJDKOutput8)
        )

        assertEquals(
            JavaVersion(true, Version.parse("9.9")),
            parseJV(openJDKOutput9)
        )

        assertEquals(
            JavaVersion(false, Version.parse("9.9")),
            parseJV(oracleOutput)
        )
    }

    private fun parseJV(s: String) = JavaVersion.parseJavaVersion(s)

    @Test
    fun testCompareJava(){
        assertTrue(
            parseJV(openJDKOutput8).compareTo(parseJV(oracleOutput)) < 0
        )

        assertTrue(parseJV(openJDKOutput8).compareTo(oracleJava8_131) == 0)
        assertTrue(parseJV(openJDKOutput8) > oracleJava8)
        assertTrue(parseJV(openJDKOutput8) >= oracleJava8)
        assertTrue(parseJV(oracleOutput) >= oracleJava8)
        assertTrue(oracleJava8_131 >= oracleJava8)
        assertTrue(parseJV(oracleOutput) >= parseJV(openJDKOutput8))
    }

    @Test
    fun testCompareVersions() {
        assertTrue(parse("1.2") == parse("1.2"))
        assertTrue(parse("1.2") >= parse("1.2"))
        assertTrue(parse("1.2") <= parse("1.2"))
        assertTrue(parse("1") < parse("1.2"))
        assertTrue(parse("1.2.3") > parse("1.2"))
        assertTrue(parse("1.2") < parse("1.2.3") )
        assertTrue(parse("1.a") < parse("1.b") )
        assertTrue(parse("1.a") <= parse("1.a") )
        assertTrue(parse("1.a") == parse("1.a") )
    }

    @Test
    fun parseVersionRange() {
        assertTrue(range("^1.8") == parse("1.8") .. MAX )
        assertTrue(range("[1.8,1.9)") == parse("1.8") .. parse("1.9") )
        assertTrue(range("[1.8,)") == parse("1.8") .. MAX )
        assertTrue(range("[1.8,1.9]") == parse("1.8") .. parse("1.9") )

    }

    fun range(s: String): ClosedRange<Version> {
        return VersionRange.parse(s)
    }
}

