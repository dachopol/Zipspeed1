package com.anakinyoo.testspeed

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ScrollView

class MainActivity : Activity() {
    private val engine = SpeedTestEngine()
    private lateinit var speedView: SpeedTestView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speedView = SpeedTestView(this)
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
            addView(
                speedView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        setContentView(scroll)

        speedView.onToggleTest = {
            if (engine.isRunning()) {
                engine.cancel()
            } else {
                speedView.resetForStart()
                runTest()
            }
        }
    }

    private fun runTest() {
        engine.start(object : SpeedTestEngine.Callback {
            override fun onPhase(phase: SpeedTestEngine.Phase) = ui {
                speedView.setPhase(
                    when (phase) {
                        SpeedTestEngine.Phase.METADATA -> SpeedTestView.UiPhase.METADATA
                        SpeedTestEngine.Phase.LATENCY -> SpeedTestView.UiPhase.LATENCY
                        SpeedTestEngine.Phase.DOWNLOAD -> SpeedTestView.UiPhase.DOWNLOAD
                        SpeedTestEngine.Phase.UPLOAD -> SpeedTestView.UiPhase.UPLOAD
                    }
                )
            }

            override fun onMetadata(metadata: SpeedTestEngine.NetworkMetadata) = ui {
                speedView.setMetadata(metadata)
            }

            override fun onLatencyProgress(latencyMs: Double, sample: Int, total: Int) = ui {
                speedView.setLatency(latencyMs)
            }

            override fun onDownloadProgress(mbps: Double, bytes: Long, totalBytes: Long) = ui {
                speedView.setDownload(mbps, bytes, totalBytes)
            }

            override fun onUploadProgress(mbps: Double, bytes: Long, totalBytes: Long) = ui {
                speedView.setUpload(mbps, bytes, totalBytes)
            }

            override fun onComplete(result: SpeedTestEngine.Result) = ui {
                speedView.setComplete(result)
            }

            override fun onError(message: String) = ui {
                speedView.setError(message)
            }

            override fun onCancelled() = ui {
                speedView.setCancelled()
            }
        })
    }

    private fun ui(block: () -> Unit) {
        runOnUiThread(block)
    }

    override fun onStop() {
        if (engine.isRunning()) engine.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        engine.shutdown()
        super.onDestroy()
    }
}
