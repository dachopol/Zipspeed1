package com.anakinyoo.testspeed

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class SpeedTestEngine(
    private val downloadBytes: Int = DEFAULT_DOWNLOAD_BYTES,
    private val uploadBytes: Int = DEFAULT_UPLOAD_BYTES,
    private val latencySamples: Int = DEFAULT_LATENCY_SAMPLES
) {
    data class NetworkMetadata(
        val ip: String? = null,
        val isp: String? = null,
        val edgeCode: String? = null,
        val city: String? = null,
        val country: String? = null
    )

    data class Result(
        val latencyMs: Double,
        val jitterMs: Double,
        val downloadMbps: Double,
        val uploadMbps: Double,
        val downloadedBytes: Long,
        val uploadedBytes: Long,
        val metadata: NetworkMetadata?
    )

    enum class Phase { METADATA, LATENCY, DOWNLOAD, UPLOAD }

    interface Callback {
        fun onPhase(phase: Phase)
        fun onMetadata(metadata: NetworkMetadata)
        fun onLatencyProgress(latencyMs: Double, sample: Int, total: Int)
        fun onDownloadProgress(mbps: Double, bytes: Long, totalBytes: Long)
        fun onUploadProgress(mbps: Double, bytes: Long, totalBytes: Long)
        fun onComplete(result: Result)
        fun onError(message: String)
        fun onCancelled()
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val cancelled = AtomicBoolean(false)
    private val running = AtomicBoolean(false)

    @Volatile
    private var activeConnection: HttpURLConnection? = null

    init {
        require(downloadBytes > 0) { "downloadBytes must be > 0" }
        require(uploadBytes > 0) { "uploadBytes must be > 0" }
        require(latencySamples >= 2) { "latencySamples must be >= 2" }
    }

    fun isRunning(): Boolean = running.get()

    fun start(callback: Callback): Boolean {
        if (!running.compareAndSet(false, true)) return false
        cancelled.set(false)

        executor.execute {
            try {
                callback.onPhase(Phase.METADATA)
                val metadata = runCatching { fetchMetadata() }.getOrNull()
                metadata?.let(callback::onMetadata)
                checkCancelled()

                callback.onPhase(Phase.LATENCY)
                val latencies = measureLatency(callback)
                val latency = SpeedMath.average(latencies)
                    ?: throw IllegalStateException("Latency measurement returned no samples")
                val jitter = SpeedMath.standardDeviation(latencies)
                    ?: throw IllegalStateException("Jitter measurement returned no samples")
                checkCancelled()

                callback.onPhase(Phase.DOWNLOAD)
                val download = measureDownload(callback)
                checkCancelled()

                callback.onPhase(Phase.UPLOAD)
                val upload = measureUpload(callback)
                checkCancelled()

                callback.onComplete(
                    Result(
                        latencyMs = latency,
                        jitterMs = jitter,
                        downloadMbps = download.first,
                        uploadMbps = upload.first,
                        downloadedBytes = download.second,
                        uploadedBytes = upload.second,
                        metadata = metadata
                    )
                )
            } catch (_: CancelledException) {
                callback.onCancelled()
            } catch (t: Throwable) {
                if (cancelled.get()) callback.onCancelled()
                else callback.onError(safeErrorMessage(t))
            } finally {
                activeConnection?.disconnect()
                activeConnection = null
                running.set(false)
            }
        }
        return true
    }

    fun cancel() {
        cancelled.set(true)
        activeConnection?.disconnect()
    }

    fun shutdown() {
        cancel()
        executor.shutdownNow()
    }

    private fun fetchMetadata(): NetworkMetadata {
        val connection = open(METADATA_URL, "GET")
        activeConnection = connection
        return try {
            val code = connection.responseCode
            requireSuccessful(code, "Metadata")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            NetworkMetadata(
                ip = jsonString(body, "clientIp"),
                isp = jsonString(body, "asOrganization"),
                edgeCode = jsonString(body, "colo"),
                city = jsonString(body, "city"),
                country = jsonString(body, "country")
            )
        } finally {
            connection.disconnect()
            if (activeConnection === connection) activeConnection = null
        }
    }

    private fun measureLatency(callback: Callback): List<Double> {
        val values = ArrayList<Double>(latencySamples)
        repeat(latencySamples) { index ->
            checkCancelled()
            val url = "$DOWNLOAD_URL?bytes=1&ts=${System.nanoTime()}"
            val connection = open(url, "GET")
            activeConnection = connection
            try {
                val start = System.nanoTime()
                val code = connection.responseCode
                requireSuccessful(code, "Latency")
                var bytesRead = 0L
                connection.inputStream.use { input ->
                    val buffer = ByteArray(16)
                    while (true) {
                        checkCancelled()
                        val n = input.read(buffer)
                        if (n < 0) break
                        bytesRead += n
                    }
                }
                require(bytesRead == 1L) { "Latency payload incomplete: $bytesRead/1 byte" }
                val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
                values += elapsedMs
                callback.onLatencyProgress(elapsedMs, index + 1, latencySamples)
            } finally {
                connection.disconnect()
                if (activeConnection === connection) activeConnection = null
            }
            if (index < latencySamples - 1) Thread.sleep(LATENCY_SAMPLE_GAP_MS)
        }
        return values
    }

    private fun measureDownload(callback: Callback): Pair<Double, Long> {
        val url = "$DOWNLOAD_URL?bytes=$downloadBytes&ts=${System.nanoTime()}"
        val connection = open(url, "GET")
        activeConnection = connection
        try {
            val code = connection.responseCode
            requireSuccessful(code, "Download")

            var bytesRead = 0L
            val buffer = ByteArray(IO_BUFFER_BYTES)
            val start = System.nanoTime()
            var lastUi = start

            BufferedInputStream(connection.inputStream, buffer.size).use { input ->
                while (true) {
                    checkCancelled()
                    val n = input.read(buffer)
                    if (n < 0) break
                    bytesRead += n
                    val now = System.nanoTime()
                    if (now - lastUi >= UI_UPDATE_INTERVAL_NS) {
                        callback.onDownloadProgress(
                            SpeedMath.mbps(bytesRead, max(1L, now - start)),
                            bytesRead,
                            downloadBytes.toLong()
                        )
                        lastUi = now
                    }
                }
            }

            val elapsed = max(1L, System.nanoTime() - start)
            require(bytesRead == downloadBytes.toLong()) {
                "Download payload incomplete: $bytesRead/$downloadBytes bytes"
            }
            val mbps = SpeedMath.mbps(bytesRead, elapsed)
            callback.onDownloadProgress(mbps, bytesRead, downloadBytes.toLong())
            return mbps to bytesRead
        } finally {
            connection.disconnect()
            if (activeConnection === connection) activeConnection = null
        }
    }

    private fun measureUpload(callback: Callback): Pair<Double, Long> {
        val connection = open(UPLOAD_URL, "POST")
        activeConnection = connection
        try {
            connection.doOutput = true
            connection.setFixedLengthStreamingMode(uploadBytes)
            connection.setRequestProperty("Content-Type", "application/octet-stream")

            val chunk = ByteArray(IO_BUFFER_BYTES)
            var sent = 0L
            val start = System.nanoTime()
            var lastUi = start

            BufferedOutputStream(connection.outputStream, chunk.size).use { out ->
                while (sent < uploadBytes) {
                    checkCancelled()
                    val remaining = uploadBytes - sent.toInt()
                    val size = minOf(chunk.size, remaining)
                    out.write(chunk, 0, size)
                    sent += size
                    val now = System.nanoTime()
                    if (now - lastUi >= UI_UPDATE_INTERVAL_NS) {
                        callback.onUploadProgress(
                            SpeedMath.mbps(sent, max(1L, now - start)),
                            sent,
                            uploadBytes.toLong()
                        )
                        lastUi = now
                    }
                }
                out.flush()
            }
            val sendElapsed = max(1L, System.nanoTime() - start)
            require(sent == uploadBytes.toLong()) { "Upload payload incomplete: $sent/$uploadBytes bytes" }

            val code = connection.responseCode
            requireSuccessful(code, "Upload")
            connection.inputStream.use { input ->
                val buffer = ByteArray(1024)
                while (true) {
                    checkCancelled()
                    if (input.read(buffer) < 0) break
                }
            }

            val mbps = SpeedMath.mbps(sent, sendElapsed)
            callback.onUploadProgress(mbps, sent, uploadBytes.toLong())
            return mbps to sent
        } finally {
            connection.disconnect()
            if (activeConnection === connection) activeConnection = null
        }
    }

    private fun open(url: String, method: String): HttpURLConnection =
        (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            useCaches = false
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Accept-Encoding", "identity")
            setRequestProperty("Cache-Control", "no-store, no-cache")
            setRequestProperty("Pragma", "no-cache")
            setRequestProperty("User-Agent", USER_AGENT)
        }

    private fun requireSuccessful(code: Int, phase: String) {
        require(code in 200..299) { "$phase HTTP $code" }
    }

    private fun checkCancelled() {
        if (cancelled.get() || Thread.currentThread().isInterrupted) throw CancelledException()
    }

    private fun safeErrorMessage(t: Throwable): String {
        val text = t.message?.trim().orEmpty()
        return if (text.isNotEmpty()) text else t.javaClass.simpleName.ifBlank { "Measurement failed" }
    }

    private fun jsonString(json: String, key: String): String? {
        val pattern = Regex("\\\"${Regex.escape(key)}\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
        val raw = pattern.find(json)?.groupValues?.getOrNull(1) ?: return null
        return raw
            .replace("\\\\\"", "\"")
            .replace("\\\\/", "/")
            .replace("\\\\n", "\n")
            .replace("\\\\t", "\t")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    private class CancelledException : RuntimeException()

    companion object {
        const val DEFAULT_DOWNLOAD_BYTES = 20 * 1024 * 1024
        const val DEFAULT_UPLOAD_BYTES = 5 * 1024 * 1024
        const val DEFAULT_LATENCY_SAMPLES = 5

        private const val METADATA_URL = "https://speed.cloudflare.com/meta"
        private const val DOWNLOAD_URL = "https://speed.cloudflare.com/__down"
        private const val UPLOAD_URL = "https://speed.cloudflare.com/__up"
        private const val USER_AGENT = "TestSpeedByAnakinYoo/1.1.0"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 30_000
        private const val LATENCY_SAMPLE_GAP_MS = 120L
        private const val IO_BUFFER_BYTES = 64 * 1024
        private const val UI_UPDATE_INTERVAL_NS = 120_000_000L
    }
}
