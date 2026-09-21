package com.anakinyoo.testspeed

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakeDataGuardTest {
    private fun sourceRoot(): File {
        val root = listOf(File("src/main"), File("app/src/main")).firstOrNull { it.exists() }
        assertNotNull("Production source root not found; guard test must not silently skip", root)
        return root!!
    }

    @Test
    fun productionSourceDoesNotUseRandomMetrics() {
        val forbidden = listOf(
            "kotlin.random.Random",
            "Math.random()",
            "java.util.Random",
            "ThreadLocalRandom"
        )
        sourceRoot().walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .forEach { file ->
                val text = file.readText()
                forbidden.forEach { token ->
                    assertFalse("Forbidden fake-data token '$token' in ${file.path}", text.contains(token))
                }
            }
    }

    @Test
    fun measurementEndpointsAreHttpsAndRawByteCountingIsRequested() {
        val engine = sourceRoot()
            .walkTopDown()
            .first { it.isFile && it.name == "SpeedTestEngine.kt" }
            .readText()

        assertFalse("Cleartext speed-test endpoint found", engine.contains("http://"))
        assertTrue("Download must request identity encoding for byte integrity", engine.contains("Accept-Encoding\", \"identity"))
        assertTrue("Download must reject incomplete payloads", engine.contains("Download payload incomplete"))
        assertTrue("Upload must reject incomplete payloads", engine.contains("Upload payload incomplete"))
    }

    @Test
    fun bannedSyntheticServerSignalsAreAbsent() {
        val allSource = sourceRoot().walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .joinToString("\n") { it.readText() }

        assertFalse("cf-ray must not be used as server/location truth", allSource.contains("cf-ray", ignoreCase = true))
        assertFalse("Synthetic ping label must not be used for HTTP latency", allSource.contains("fakePing", ignoreCase = true))
    }
}
