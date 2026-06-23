package com.example.spinwheel.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class ColorSwatchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
    }
    private val selectedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(2.4f)
    }
    private val rainbowColors = intArrayOf(
        Color.RED,
        Color.MAGENTA,
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.YELLOW,
        Color.RED,
    )

    var swatchColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var isRainbow: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var isChecked: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    fun colorAt(x: Float, y: Float): Int {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f
        val dx = x - cx
        val dy = y - cy
        val hue = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360.0) % 360.0).toFloat()
        val saturation = (hypot(dx, dy) / radius).coerceIn(0f, 1f)
        return Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - dp(1f)
        val fillRadius = if (isChecked) radius - dp(6f) else radius - dp(1f)

        if (isChecked) {
            fillPaint.shader = null
            fillPaint.color = swatchColor
            canvas.drawCircle(cx, cy, radius, fillPaint)
        }

        if (isRainbow) {
            fillPaint.shader = SweepGradient(cx, cy, rainbowColors, null)
        } else {
            fillPaint.shader = null
            fillPaint.color = swatchColor
        }
        canvas.drawCircle(cx, cy, fillRadius, fillPaint)
        fillPaint.shader = null

        if (isChecked) {
            canvas.drawCircle(cx, cy, fillRadius - dp(3f), selectedStrokePaint)
        } else {
            strokePaint.color = Color.argb(38, 15, 7, 5)
            canvas.drawCircle(cx, cy, fillRadius, strokePaint)
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
