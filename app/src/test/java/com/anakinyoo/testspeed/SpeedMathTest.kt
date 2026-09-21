package com.anakinyoo.testspeed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpeedMathTest {
    @Test
    fun mbpsUsesActualBytesAndElapsedTime() {
        val result = SpeedMath.mbps(bytes = 10_000_000, elapsedNanos = 1_000_000_000)
        assertEquals(80.0, result, 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mbpsRejectsZeroElapsedTime() {
        SpeedMath.mbps(bytes = 1, elapsedNanos = 0)
    }

    @Test
    fun averageUsesMeasuredSamples() {
        assertEquals(20.0, SpeedMath.average(listOf(10.0, 20.0, 30.0))!!, 0.0001)
    }

    @Test
    fun standardDeviationIsCalculatedFromSamples() {
        val result = SpeedMath.standardDeviation(listOf(10.0, 20.0, 30.0))
        assertNotNull(result)
        assertEquals(8.1649, result!!, 0.001)
    }
}
