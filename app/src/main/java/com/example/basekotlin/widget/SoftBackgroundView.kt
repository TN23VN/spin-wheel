package com.example.spinwheel.widget

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SoftBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#FFFBFB"))

        paint.maskFilter = BlurMaskFilter(dp(72f), BlurMaskFilter.Blur.NORMAL)
        drawGlow(canvas, width * 0.12f, height * 0.28f, dp(110f), "#24FFD6D5")
        drawGlow(canvas, width * 0.92f, height * 0.46f, dp(120f), "#20FFB4A9")
        drawGlow(canvas, width * 0.82f, height * 0.92f, dp(140f), "#26FFD6B8")
        paint.maskFilter = null
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: String) {
        paint.color = Color.parseColor(color)
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
