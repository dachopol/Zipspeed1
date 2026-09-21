package com.anakinyoo.testspeed

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin

class SpeedTestView(context: Context) : View(context) {
    enum class UiPhase { IDLE, METADATA, LATENCY, DOWNLOAD, UPLOAD, DONE, ERROR, CANCELLED }

    var onToggleTest: (() -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val bg = if (isDark) Color.rgb(7, 11, 19) else Color.rgb(244, 247, 252)
    private val panel = if (isDark) Color.rgb(15, 23, 38) else Color.WHITE
    private val text = if (isDark) Color.rgb(239, 245, 255) else Color.rgb(18, 26, 39)
    private val muted = if (isDark) Color.rgb(142, 156, 181) else Color.rgb(95, 107, 127)
    private val cyan = Color.rgb(57, 232, 202)
    private val violet = Color.rgb(131, 111, 255)
    private val warning = Color.rgb(255, 180, 78)
    private val danger = Color.rgb(255, 98, 121)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val goRect = RectF()

    private var phase = UiPhase.IDLE
    private var liveMbps: Double? = null
    private var latencyMs: Double? = null
    private var jitterMs: Double? = null
    private var downloadMbps: Double? = null
    private var uploadMbps: Double? = null
    private var progress = 0f
    private var ip: String? = null
    private var isp: String? = null
    private var edge: String? = null
    private var clientArea: String? = null
    private var error: String? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Internet speed test. Tap GO to start."
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val desiredH = (860 * density).toInt()
        val h = maxOf(desiredH, MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bg)
        val w = width.toFloat()
        val pad = 20f * density

        drawHeader(canvas, pad)

        val gaugeTop = 94f * density
        val gaugeSize = min(w - pad * 2, 330f * density)
        val cx = w / 2f
        val cy = gaugeTop + gaugeSize * 0.54f
        val radius = gaugeSize * 0.42f
        drawGauge(canvas, cx, cy, radius)

        val cardsTop = gaugeTop + gaugeSize * 0.88f
        drawMetricCards(canvas, pad, cardsTop, w - pad * 2)

        val infoTop = cardsTop + 188f * density
        drawInfoPanel(canvas, pad, infoTop, w - pad * 2)

        val footerTop = infoTop + 220f * density
        drawFooter(canvas, pad, footerTop)
    }

    private fun drawHeader(canvas: Canvas, pad: Float) {
        paint.clearShadowLayer()
        paint.color = text
        paint.textSize = 24f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TEST SPEED", pad, 38f * density, paint)

        paint.color = cyan
        paint.textSize = 12f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("by AnakinYoo", pad, 58f * density, paint)

        paint.color = muted
        paint.textSize = 12f * density
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(statusText(), pad, 80f * density, paint)
    }

    private fun drawGauge(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val arc = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 14f * density
        paint.color = if (isDark) Color.rgb(31, 44, 65) else Color.rgb(219, 227, 239)
        canvas.drawArc(arc, 150f, 240f, false, paint)

        paint.color = cyan
        paint.setShadowLayer(14f * density, 0f, 0f, cyan)
        val sweep = 240f * speedNormalized() * progressForGauge()
        if (sweep > 0.5f) canvas.drawArc(arc, 150f, sweep, false, paint)
        paint.clearShadowLayer()

        for (i in 0..12) {
            val a = Math.toRadians((150f + i * 20f).toDouble())
            val inner = radius - 25f * density
            val outer = radius - 10f * density
            paint.strokeWidth = if (i % 3 == 0) 3f * density else 1.5f * density
            paint.color = if (i % 3 == 0) text else muted
            canvas.drawLine(
                cx + cos(a).toFloat() * inner,
                cy + sin(a).toFloat() * inner,
                cx + cos(a).toFloat() * outer,
                cy + sin(a).toFloat() * outer,
                paint
            )
        }

        val needleAngle = Math.toRadians((150f + 240f * speedNormalized()).toDouble())
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 5f * density
        paint.color = violet
        paint.setShadowLayer(8f * density, 0f, 0f, violet)
        canvas.drawLine(
            cx,
            cy,
            cx + cos(needleAngle).toFloat() * radius * 0.68f,
            cy + sin(needleAngle).toFloat() * radius * 0.68f,
            paint
        )
        paint.clearShadowLayer()

        paint.style = Paint.Style.FILL
        paint.color = panel
        canvas.drawCircle(cx, cy, 64f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * density
        paint.color = cyan
        canvas.drawCircle(cx, cy, 64f * density, paint)

        paint.style = Paint.Style.FILL
        paint.color = text
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 29f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val centerValue = if (phase == UiPhase.LATENCY) latencyMs else liveMbps
        val value = centerValue?.let { "%.1f".format(it) } ?: "--"
        canvas.drawText(value, cx, cy - 3f * density, paint)

        paint.color = muted
        paint.textSize = 11f * density
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(if (phase == UiPhase.LATENCY) "ms • HTTP" else "Mbps", cx, cy + 19f * density, paint)

        val btnCy = cy + radius + 54f * density
        val btnR = 34f * density
        goRect.set(cx - btnR, btnCy - btnR, cx + btnR, btnCy + btnR)
        paint.color = if (isRunning()) danger else cyan
        paint.setShadowLayer(14f * density, 0f, 4f * density, paint.color)
        canvas.drawCircle(cx, btnCy, btnR, paint)
        paint.clearShadowLayer()
        paint.color = Color.rgb(4, 17, 21)
        paint.textSize = 15f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isRunning()) "STOP" else "GO", cx, btnCy + 5f * density, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawMetricCards(canvas: Canvas, x: Float, y: Float, totalW: Float) {
        val gap = 10f * density
        val cardW = (totalW - gap) / 2f
        val cardH = 82f * density
        metricCard(canvas, x, y, cardW, cardH, "DOWNLOAD", downloadMbps?.let { "%.1f Mbps".format(it) } ?: "--", cyan)
        metricCard(canvas, x + cardW + gap, y, cardW, cardH, "UPLOAD", uploadMbps?.let { "%.1f Mbps".format(it) } ?: "--", violet)
        metricCard(canvas, x, y + cardH + gap, cardW, cardH, "LATENCY", latencyMs?.let { "%.1f ms".format(it) } ?: "--", warning)
        metricCard(canvas, x + cardW + gap, y + cardH + gap, cardW, cardH, "JITTER", jitterMs?.let { "%.1f ms".format(it) } ?: "--", warning)
    }

    private fun metricCard(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, label: String, value: String, accent: Int) {
        val r = RectF(x, y, x + w, y + h)
        paint.style = Paint.Style.FILL
        paint.color = panel
        paint.setShadowLayer(8f * density, 0f, 3f * density, Color.argb(if (isDark) 80 else 30, 0, 0, 0))
        canvas.drawRoundRect(r, 18f * density, 18f * density, paint)
        paint.clearShadowLayer()

        paint.color = accent
        canvas.drawRoundRect(RectF(x + 13f * density, y + 14f * density, x + 17f * density, y + h - 14f * density), 2f * density, 2f * density, paint)

        paint.color = muted
        paint.textSize = 10f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, x + 28f * density, y + 29f * density, paint)

        paint.color = text
        paint.textSize = 19f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 28f * density, y + 56f * density, paint)
    }

    private fun drawInfoPanel(canvas: Canvas, x: Float, y: Float, w: Float) {
        val h = 204f * density
        paint.color = panel
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 18f * density, 18f * density, paint)

        paint.color = text
        paint.textSize = 14f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NETWORK", x + 16f * density, y + 25f * density, paint)

        infoLine(canvas, "IP", ip ?: "--", x + 16f * density, y + 53f * density, w - 32f * density)
        infoLine(canvas, "ISP", isp ?: "--", x + 16f * density, y + 81f * density, w - 32f * density)
        infoLine(canvas, "TEST PROVIDER", "Cloudflare", x + 16f * density, y + 109f * density, w - 32f * density)
        infoLine(canvas, "EDGE", edge ?: "--", x + 16f * density, y + 137f * density, w - 32f * density)
        infoLine(canvas, "CLIENT AREA", clientArea ?: "--", x + 16f * density, y + 165f * density, w - 32f * density)

        if (!error.isNullOrBlank()) {
            paint.color = danger
            paint.textSize = 10.5f * density
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(ellipsize(error!!, 54), x + 16f * density, y + 190f * density, paint)
        }
    }

    private fun infoLine(canvas: Canvas, label: String, value: String, x: Float, y: Float, w: Float) {
        paint.color = muted
        paint.textSize = 11f * density
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x, y, paint)

        paint.color = text
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(ellipsize(value, 38), x + w, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawFooter(canvas: Canvas, x: Float, y: Float) {
        paint.color = muted
        paint.textSize = 10.5f * density
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Single-stream HTTP measurement • no random results", x, y, paint)
        canvas.drawText("Default payload: 20 MiB down • 5 MiB up", x, y + 20f * density, paint)
        canvas.drawText("Client area is provider metadata, not GPS", x, y + 40f * density, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && goRect.contains(event.x, event.y)) {
            performClick()
            onToggleTest?.invoke()
            return true
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun resetForStart() {
        phase = UiPhase.METADATA
        liveMbps = null
        latencyMs = null
        jitterMs = null
        downloadMbps = null
        uploadMbps = null
        progress = 0f
        ip = null
        isp = null
        edge = null
        clientArea = null
        error = null
        invalidate()
    }

    fun setPhase(value: UiPhase) {
        phase = value
        liveMbps = if (value == UiPhase.DOWNLOAD || value == UiPhase.UPLOAD) 0.0 else liveMbps
        progress = when (value) {
            UiPhase.DOWNLOAD, UiPhase.UPLOAD -> 0f
            else -> progress
        }
        invalidate()
    }

    fun setMetadata(metadata: SpeedTestEngine.NetworkMetadata) {
        ip = metadata.ip
        isp = metadata.isp
        edge = metadata.edgeCode?.takeIf { it.isNotBlank() }
        clientArea = listOfNotNull(metadata.city, metadata.country)
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" • ")
        invalidate()
    }

    fun setLatency(value: Double) {
        latencyMs = value
        liveMbps = null
        invalidate()
    }

    fun setDownload(value: Double, done: Long, total: Long) {
        liveMbps = value
        downloadMbps = value
        progress = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
        invalidate()
    }

    fun setUpload(value: Double, done: Long, total: Long) {
        liveMbps = value
        uploadMbps = value
        progress = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
        invalidate()
    }

    fun setComplete(result: SpeedTestEngine.Result) {
        phase = UiPhase.DONE
        latencyMs = result.latencyMs
        jitterMs = result.jitterMs
        downloadMbps = result.downloadMbps
        uploadMbps = result.uploadMbps
        liveMbps = result.downloadMbps
        progress = 1f
        result.metadata?.let(::setMetadata)
        error = null
        invalidate()
    }

    fun setError(message: String) {
        phase = UiPhase.ERROR
        error = message
        liveMbps = null
        progress = 0f
        invalidate()
    }

    fun setCancelled() {
        phase = UiPhase.CANCELLED
        liveMbps = null
        progress = 0f
        invalidate()
    }

    private fun isRunning(): Boolean = phase in setOf(UiPhase.METADATA, UiPhase.LATENCY, UiPhase.DOWNLOAD, UiPhase.UPLOAD)

    private fun speedNormalized(): Float {
        val speed = liveMbps ?: when (phase) {
            UiPhase.DONE -> downloadMbps
            else -> null
        } ?: return 0f
        val clamped = speed.coerceAtLeast(0.0)
        return (ln(1.0 + clamped) / ln(1001.0)).toFloat().coerceIn(0f, 1f)
    }

    private fun progressForGauge(): Float = if (isRunning()) progress.coerceAtLeast(0.12f) else 1f

    private fun statusText(): String = when (phase) {
        UiPhase.IDLE -> "Ready • tap GO"
        UiPhase.METADATA -> "Reading network metadata…"
        UiPhase.LATENCY -> "Measuring HTTP latency…"
        UiPhase.DOWNLOAD -> "Measuring download…"
        UiPhase.UPLOAD -> "Measuring upload…"
        UiPhase.DONE -> "Test complete"
        UiPhase.ERROR -> "Measurement error"
        UiPhase.CANCELLED -> "Test stopped"
    }

    private fun ellipsize(value: String, max: Int): String =
        if (value.length <= max) value else value.take(max - 1) + "…"
}
