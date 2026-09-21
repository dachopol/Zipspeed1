package com.anakinyoo.testspeed

import kotlin.math.sqrt

object SpeedMath {
    fun mbps(bytes: Long, elapsedNanos: Long): Double {
        require(bytes >= 0) { "bytes must be >= 0" }
        require(elapsedNanos > 0) { "elapsedNanos must be > 0" }
        val seconds = elapsedNanos / 1_000_000_000.0
        return (bytes * 8.0) / seconds / 1_000_000.0
    }

    fun average(values: List<Double>): Double? =
        if (values.isEmpty()) null else values.sum() / values.size

    fun standardDeviation(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        val mean = average(values) ?: return null
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        return sqrt(variance)
    }
}
