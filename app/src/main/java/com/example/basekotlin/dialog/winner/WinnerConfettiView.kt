package com.example.spinwheel.dialog.winner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WinnerConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        canvas.save()
        canvas.scale(width / DESIGN_WIDTH, height / DESIGN_HEIGHT)
        drawLeftTop(canvas)
        drawLeftSide(canvas)
        drawCenter(canvas)
        drawRightSide(canvas)
        canvas.restore()
    }

    private fun drawLeftTop(canvas: Canvas) {
        drawStair(canvas, 72f, 27f, "#FF404B", 6, 7f, 7f, 6f, 6f)
        block(canvas, 36f, 104f, 24f, 8f, "#2F8DFF")
        block(canvas, 58f, 101f, 8f, 4f, "#2F8DFF")
        block(canvas, 283f, 13f, 10f, 8f, "#FF4B55")
        block(canvas, 208f, 201f, 34f, 8f, "#FF404B")
        block(canvas, 241f, 194f, 8f, 4f, "#FF404B")
    }

    private fun drawLeftSide(canvas: Canvas) {
        drawStair(canvas, 60f, 282f, "#B469FF", 4, 6f, -7f, 5f, 5f)
        drawRibbon(canvas, 60f, 306f, "#FFC24A", 8)
        block(canvas, 82f, 375f, 5f, 32f, "#FFC24A")
        block(canvas, 88f, 406f, 8f, 4f, "#2F8DFF")
        block(canvas, 744f, 271f, 5f, 5f, "#2F8DFF")
    }

    private fun drawCenter(canvas: Canvas) {
        drawStair(canvas, 411f, 162f, "#4FCD54", 4, 6f, 8f, 7f, 7f)
        block(canvas, 343f, 383f, 10f, 33f, "#8C31B5")
        block(canvas, 448f, 393f, 14f, 38f, "#8C31B5")
        block(canvas, 462f, 414f, 11f, 26f, "#8C31B5")
        block(canvas, 454f, 431f, 14f, 9f, "#8C31B5")
    }

    private fun drawRightSide(canvas: Canvas) {
        block(canvas, 608f, 78f, 25f, 4f, "#2F8DFF")
        drawStair(canvas, 507f, 93f, "#FF404B", 5, 9f, 0f, 7f, 5f)
        block(canvas, 563f, 174f, 35f, 5f, "#F4582D")
        block(canvas, 596f, 179f, 20f, 5f, "#F4582D")
        block(canvas, 614f, 184f, 10f, 5f, "#F4582D")
        block(canvas, 621f, 190f, 6f, 30f, "#F4582D")
        block(canvas, 612f, 219f, 7f, 18f, "#F4582D")
        block(canvas, 619f, 238f, 16f, 6f, "#F4582D")
        block(canvas, 574f, 306f, 22f, 7f, "#8C31B5")
        block(canvas, 594f, 313f, 10f, 12f, "#8C31B5")
    }

    private fun drawStair(
        canvas: Canvas,
        x: Float,
        y: Float,
        color: String,
        count: Int,
        dx: Float,
        dy: Float,
        w: Float,
        h: Float,
    ) {
        repeat(count) { index ->
            block(canvas, x + index * dx, y + index * dy, w, h, color)
        }
    }

    private fun drawRibbon(canvas: Canvas, x: Float, y: Float, color: String, count: Int) {
        repeat(count) { index ->
            val offset = if (index % 2 == 0) 0f else 12f
            block(canvas, x + offset, y + index * 9f, 8f, 18f, color)
        }
    }

    private fun block(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, color: String) {
        paint.color = Color.parseColor(color)
        canvas.drawRect(x, y, x + w, y + h, paint)
    }

    companion object {
        private const val DESIGN_WIDTH = 760f
        private const val DESIGN_HEIGHT = 584f
    }
}
