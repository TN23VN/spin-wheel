package com.example.spinwheel.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class ColorFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var onColorChanged: ((Float, Float) -> Unit)? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val markerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
    }
    private val rect = RectF()
    private val clipPath = Path()
    private var hue = 0f
    private var saturation = 1f
    private var value = 1f

    fun setColor(hue: Float, saturation: Float, value: Float) {
        this.hue = hue
        this.saturation = saturation.coerceIn(0f, 1f)
        this.value = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val radius = dp(16f)
        clipPath.reset()
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)
        paint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            0f,
            Color.WHITE,
            Color.HSVToColor(floatArrayOf(hue, 1f, 1f)),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(rect, paint)

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            Color.TRANSPARENT,
            Color.BLACK,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(rect, paint)
        paint.shader = null
        canvas.restore()

        val markerX = saturation * width
        val markerY = (1f - value) * height
        canvas.drawCircle(markerX, markerY, dp(5f), markerPaint)
        canvas.drawCircle(markerX, markerY, dp(5f), markerStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN ||
            event.action == MotionEvent.ACTION_MOVE ||
            event.action == MotionEvent.ACTION_UP
        ) {
            saturation = (event.x / width).coerceIn(0f, 1f)
            value = (1f - event.y / height).coerceIn(0f, 1f)
            onColorChanged?.invoke(saturation, value)
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

class HueSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var onHueChanged: ((Float) -> Unit)? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(145, 15, 7, 5)
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
    }
    private val rect = RectF()
    private var hue = 0f

    fun setHue(hue: Float) {
        this.hue = hue.coerceIn(0f, 360f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val thumbRadius = dp(11f)
        val barHeight = dp(10f)
        val centerY = height / 2f
        rect.set(thumbRadius, centerY - barHeight / 2f, width - thumbRadius, centerY + barHeight / 2f)
        paint.shader = LinearGradient(
            rect.left,
            0f,
            rect.right,
            0f,
            HUE_COLORS,
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(rect, barHeight / 2f, barHeight / 2f, paint)
        paint.shader = null

        val thumbX = rect.left + hue / 360f * rect.width()
        canvas.drawCircle(thumbX, centerY, thumbRadius, thumbPaint)
        canvas.drawCircle(thumbX, centerY, thumbRadius, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN ||
            event.action == MotionEvent.ACTION_MOVE ||
            event.action == MotionEvent.ACTION_UP
        ) {
            val thumbRadius = dp(11f)
            val usableWidth = (width - thumbRadius * 2f).coerceAtLeast(1f)
            hue = ((event.x - thumbRadius) / usableWidth).coerceIn(0f, 1f) * 360f
            onHueChanged?.invoke(hue)
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    companion object {
        private val HUE_COLORS = intArrayOf(
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA,
            Color.RED,
        )
    }
}

class AlphaSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var onAlphaChanged: ((Int) -> Unit)? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(145, 15, 7, 5)
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
    }
    private val rect = RectF()
    private var baseColor = Color.RED
    private var alpha = 255

    fun setColor(color: Int, alpha: Int) {
        baseColor = color
        this.alpha = alpha.coerceIn(0, 255)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val thumbRadius = dp(11f)
        val barHeight = dp(18f)
        val centerY = height / 2f
        rect.set(thumbRadius, centerY - barHeight / 2f, width - thumbRadius, centerY + barHeight / 2f)

        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(rect, barHeight / 2f, barHeight / 2f, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        drawCheckerboard(canvas, rect)

        val opaqueColor = Color.rgb(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        val transparentColor = Color.argb(0, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        paint.shader = LinearGradient(rect.left, 0f, rect.right, 0f, transparentColor, opaqueColor, Shader.TileMode.CLAMP)
        canvas.drawRect(rect, paint)
        paint.shader = null
        canvas.restore()

        val thumbX = rect.left + alpha / 255f * rect.width()
        canvas.drawCircle(thumbX, centerY, thumbRadius, thumbPaint)
        canvas.drawCircle(thumbX, centerY, thumbRadius, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN ||
            event.action == MotionEvent.ACTION_MOVE ||
            event.action == MotionEvent.ACTION_UP
        ) {
            val thumbRadius = dp(11f)
            val usableWidth = (width - thumbRadius * 2f).coerceAtLeast(1f)
            alpha = (((event.x - thumbRadius) / usableWidth).coerceIn(0f, 1f) * 255f).roundToInt()
            onAlphaChanged?.invoke(alpha)
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun drawCheckerboard(canvas: Canvas, area: RectF) {
        val size = dp(10f)
        var y = area.top
        var row = 0
        while (y < area.bottom) {
            var x = area.left
            var column = 0
            while (x < area.right) {
                paint.shader = null
                paint.color = if ((row + column) % 2 == 0) Color.WHITE else Color.rgb(220, 220, 220)
                canvas.drawRect(x, y, (x + size).coerceAtMost(area.right), (y + size).coerceAtMost(area.bottom), paint)
                x += size
                column++
            }
            y += size
            row++
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
