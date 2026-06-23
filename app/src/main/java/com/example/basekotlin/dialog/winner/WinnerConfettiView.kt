package com.example.spinwheel.dialog.winner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

class WinnerConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val confettiRect = RectF()
    private val pieces = buildPieces()
    private var animator: ValueAnimator? = null
    private var animationProgress = 0f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGifAnimation()
    }

    override fun onDetachedFromWindow() {
        stopGifAnimation()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE && isAttachedToWindow) {
            startGifAnimation()
        } else {
            stopGifAnimation()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        canvas.save()
        canvas.scale(width / DESIGN_WIDTH, height / DESIGN_HEIGHT)
        drawConfetti(canvas)
        canvas.restore()
    }

    private fun startGifAnimation() {
        if (animator != null) return

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = GIF_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    private fun stopGifAnimation() {
        animator?.cancel()
        animator = null
    }

    private fun drawConfetti(canvas: Canvas) {
        pieces.forEach { piece ->
            val loop = (animationProgress + piece.phase) % 1f
            val angle = loop * TWO_PI
            val wave = sin(angle.toDouble()).toFloat()
            val drift = cos(angle.toDouble()).toFloat()
            val centerX = piece.x + piece.width / 2f + wave * piece.sway
            val centerY = piece.y + piece.height / 2f + drift * piece.floatRange
            val scale = 0.84f + (wave + 1f) * 0.08f

            paint.color = piece.color
            paint.alpha = (180 + (wave + 1f) * 37.5f).toInt()

            canvas.save()
            canvas.translate(centerX, centerY)
            canvas.rotate(piece.baseRotation + 360f * loop * piece.spinDirection)
            canvas.scale(scale, scale)
            confettiRect.set(
                -piece.width / 2f,
                -piece.height / 2f,
                piece.width / 2f,
                piece.height / 2f,
            )
            canvas.drawRect(confettiRect, paint)
            canvas.restore()
        }
        paint.alpha = 255
    }

    private fun buildPieces(): List<ConfettiPiece> {
        return mutableListOf<ConfettiPiece>().apply {
            addStair(72f, 27f, "#FF404B", 6, 7f, 7f, 6f, 6f)
            addBlock(36f, 104f, 24f, 8f, "#2F8DFF")
            addBlock(58f, 101f, 8f, 4f, "#2F8DFF")
            addBlock(283f, 13f, 10f, 8f, "#FF4B55")
            addBlock(208f, 201f, 34f, 8f, "#FF404B")
            addBlock(241f, 194f, 8f, 4f, "#FF404B")

            addStair(60f, 282f, "#B469FF", 4, 6f, -7f, 5f, 5f)
            addRibbon(60f, 306f, "#FFC24A", 8)
            addBlock(82f, 375f, 5f, 32f, "#FFC24A")
            addBlock(88f, 406f, 8f, 4f, "#2F8DFF")
            addBlock(744f, 271f, 5f, 5f, "#2F8DFF")

            addStair(411f, 162f, "#4FCD54", 4, 6f, 8f, 7f, 7f)
            addBlock(343f, 383f, 10f, 33f, "#8C31B5")
            addBlock(448f, 393f, 14f, 38f, "#8C31B5")
            addBlock(462f, 414f, 11f, 26f, "#8C31B5")
            addBlock(454f, 431f, 14f, 9f, "#8C31B5")

            addBlock(608f, 78f, 25f, 4f, "#2F8DFF")
            addStair(507f, 93f, "#FF404B", 5, 9f, 0f, 7f, 5f)
            addBlock(563f, 174f, 35f, 5f, "#F4582D")
            addBlock(596f, 179f, 20f, 5f, "#F4582D")
            addBlock(614f, 184f, 10f, 5f, "#F4582D")
            addBlock(621f, 190f, 6f, 30f, "#F4582D")
            addBlock(612f, 219f, 7f, 18f, "#F4582D")
            addBlock(619f, 238f, 16f, 6f, "#F4582D")
            addBlock(574f, 306f, 22f, 7f, "#8C31B5")
            addBlock(594f, 313f, 10f, 12f, "#8C31B5")
        }
    }

    private fun MutableList<ConfettiPiece>.addStair(
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
            addBlock(x + index * dx, y + index * dy, w, h, color)
        }
    }

    private fun MutableList<ConfettiPiece>.addRibbon(x: Float, y: Float, color: String, count: Int) {
        repeat(count) { index ->
            val offset = if (index % 2 == 0) 0f else 12f
            addBlock(x + offset, y + index * 9f, 8f, 18f, color)
        }
    }

    private fun MutableList<ConfettiPiece>.addBlock(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: String,
    ) {
        val index = size
        add(
            ConfettiPiece(
                x = x,
                y = y,
                width = width,
                height = height,
                color = Color.parseColor(color),
                baseRotation = ((index % 9) - 4) * 8f,
                phase = (index * 0.137f) % 1f,
                sway = 4f + index % 5 * 1.3f,
                floatRange = 5f + index % 4 * 2f,
                spinDirection = if (index % 2 == 0) 1f else -1f,
            ),
        )
    }

    private data class ConfettiPiece(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int,
        val baseRotation: Float,
        val phase: Float,
        val sway: Float,
        val floatRange: Float,
        val spinDirection: Float,
    )

    companion object {
        private const val DESIGN_WIDTH = 760f
        private const val DESIGN_HEIGHT = 584f
        private const val GIF_DURATION_MS = 1400L
        private const val TWO_PI = 6.2831855f
    }
}
