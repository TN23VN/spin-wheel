package com.example.spinwheel.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.spinwheel.R
import kotlin.math.min
import kotlin.random.Random

class ChooseNumberWinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        textAlign = Paint.Align.CENTER
    }
    private val winnerLabel = context.getString(R.string.winner_label)
    private val colors = intArrayOf(
        Color.parseColor("#D90000"),
        Color.parseColor("#0048D9"),
        Color.parseColor("#F28000"),
        Color.parseColor("#00C425"),
    )
    private var winnerPoints = emptyList<PointF>()
    private var animationProgress = 1f
    private var animator: ValueAnimator? = null

    fun showWinners(winnerCount: Int, touchPoints: List<PointF>, durationMs: Long) {
        animator?.cancel()
        visibility = VISIBLE
        animationProgress = 0f
        winnerPoints = buildWinnerPoints(winnerCount, touchPoints)
        invalidate()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                animationProgress = valueAnimator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun reset() {
        animator?.cancel()
        animator = null
        winnerPoints = emptyList()
        animationProgress = 1f
        visibility = GONE
        invalidate()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (winnerPoints.isEmpty()) return

        val baseRadius = dp(28f)
        val pulseRadius = baseRadius + dp(12f) * animationProgress
        val ringAlpha = (70 + 185 * animationProgress).toInt().coerceIn(0, 255)
        val fillAlpha = (28 + 42 * animationProgress).toInt().coerceIn(0, 90)

        winnerPoints.forEachIndexed { index, rawPoint ->
            val point = clampPoint(rawPoint, pulseRadius)
            val color = colors[index % colors.size]

            paint.style = Paint.Style.FILL
            paint.color = withAlpha(color, fillAlpha)
            canvas.drawCircle(point.x, point.y, pulseRadius, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(4f)
            paint.color = withAlpha(color, ringAlpha)
            canvas.drawCircle(point.x, point.y, baseRadius, paint)

            paint.strokeWidth = dp(2f)
            paint.color = withAlpha(color, (120 * animationProgress).toInt().coerceIn(0, 120))
            canvas.drawCircle(point.x, point.y, pulseRadius, paint)
        }

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = dp(24f)
        paint.color = Color.parseColor("#0F0705")
        canvas.drawText(winnerLabel, width / 2f, height * 0.72f, paint)
    }

    private fun buildWinnerPoints(winnerCount: Int, touchPoints: List<PointF>): List<PointF> {
        val safeWinnerCount = winnerCount.coerceIn(1, MAX_WINNERS)
        val sourcePoints = touchPoints.ifEmpty { buildFallbackPoints(safeWinnerCount + 1) }
        return sourcePoints
            .shuffled()
            .take(safeWinnerCount)
            .ifEmpty { buildFallbackPoints(safeWinnerCount) }
    }

    private fun buildFallbackPoints(count: Int): List<PointF> {
        val safeCount = count.coerceAtLeast(1)
        val safeWidth = width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        val safeHeight = height.takeIf { it > 0 } ?: (resources.displayMetrics.heightPixels / 2)
        val margin = dp(56f)
        return List(safeCount) {
            PointF(
                Random.nextFloat() * (safeWidth - margin * 2).coerceAtLeast(1f) + margin,
                Random.nextFloat() * (safeHeight * 0.56f - margin).coerceAtLeast(1f) + margin,
            )
        }
    }

    private fun clampPoint(point: PointF, radius: Float): PointF {
        val margin = radius + dp(8f)
        return PointF(
            point.x.coerceIn(margin, (width - margin).coerceAtLeast(margin)),
            point.y.coerceIn(margin, (height - margin).coerceAtLeast(margin)),
        )
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(
            alpha.coerceIn(0, 255),
            Color.red(color),
            Color.green(color),
            Color.blue(color),
        )
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private companion object {
        private const val MAX_WINNERS = 4
    }
}
