package com.example.spinwheel.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.spinwheel.model.WheelSlice
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val wedgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F0705")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C4251B")
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B59")
    }
    private val pointerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55A30000")
    }

    private val ringRect = RectF()
    private val wedgeRect = RectF()
    private val pointerPath = Path()
    private val defaultColors = intArrayOf(
        Color.parseColor("#FFBA5F"),
        Color.parseColor("#F568B4"),
        Color.parseColor("#FFFF74"),
        Color.parseColor("#CA8AF2"),
        Color.parseColor("#66F766"),
        Color.parseColor("#A6E4EF"),
    )
    private var animator: ValueAnimator? = null
    private var wheelRotation = 0f
    private var labels = listOf("Lois", "Peter", "Brian", "Glenn", "Joe", "Bonnie")
    private var colors = defaultColors.toList()
    private var fontSizeSp = 14f

    fun setSlices(items: List<WheelSlice>, fontSize: Int = 14) {
        val visible = items.filterNot { it.hidden }
        labels = visible.map { it.label.ifBlank { " " } }
        colors = visible.mapIndexed { index, slice ->
            if (slice.color == 0) defaultColors[index % defaultColors.size] else slice.color
        }
        fontSizeSp = fontSize.coerceIn(10, 24).toFloat()
        invalidate()
    }

    fun spinToRandom(onFinished: ((String) -> Unit)? = null) {
        if (labels.isEmpty()) return

        animator?.cancel()
        val targetIndex = Random.nextInt(labels.size)
        val sweep = 360f / labels.size
        val pointerAngle = -90f
        val targetCenter = targetIndex * sweep + sweep / 2f
        val normalized = ((pointerAngle - targetCenter - wheelRotation) % 360f + 360f) % 360f
        val endRotation = wheelRotation + 1440f + normalized

        animator = ValueAnimator.ofFloat(wheelRotation, endRotation).apply {
            duration = 1800L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                wheelRotation = it.animatedValue as Float
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit

                override fun onAnimationEnd(animation: Animator) {
                    wheelRotation %= 360f
                    onFinished?.invoke(labels[targetIndex])
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0 || labels.isEmpty()) return

        val pointerHeight = dp(40f)
        val radius = min(width.toFloat(), height.toFloat() - pointerHeight) / 2f - dp(8f)
        val cx = width / 2f
        val cy = (height + pointerHeight) / 2f
        val ringWidth = radius * 0.15f
        val innerRadius = radius - ringWidth

        wedgeRect.set(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)
        ringRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.save()
        canvas.rotate(wheelRotation, cx, cy)
        drawWedges(canvas, cx, cy, innerRadius)
        drawRing(canvas, cx, cy, radius, ringWidth)
        canvas.restore()

        drawPointer(canvas, cx, cy - radius + dp(2f), radius)
        drawCenter(canvas, cx, cy)
    }

    private fun drawWedges(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val sweep = 360f / labels.size
        labels.forEachIndexed { index, label ->
            val start = -90f + index * sweep
            wedgePaint.color = colors[index % colors.size]
            canvas.drawArc(wedgeRect, start, sweep, true, wedgePaint)

            val mid = start + sweep / 2f
            val radians = Math.toRadians(mid.toDouble())
            val tx = cx + cos(radians).toFloat() * radius * 0.58f
            val ty = cy + sin(radians).toFloat() * radius * 0.58f
            textPaint.textSize = dp(fontSizeSp)
            canvas.save()
            canvas.rotate(mid + 90f, tx, ty)
            canvas.drawText(label, tx, ty + textPaint.textSize / 3f, textPaint)
            canvas.restore()
        }
    }

    private fun drawRing(canvas: Canvas, cx: Float, cy: Float, radius: Float, ringWidth: Float) {
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = ringWidth
        ringPaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius - ringWidth / 2f, ringPaint)

        val segmentCount = labels.size * 2
        val segmentSweep = 360f / segmentCount
        ringPaint.color = Color.parseColor("#FF6656")
        for (i in 0 until segmentCount step 2) {
            canvas.drawArc(ringRect, -90f + i * segmentSweep, segmentSweep, false, ringPaint)
        }

        ringPaint.style = Paint.Style.FILL
        ringPaint.color = Color.parseColor("#FFB4A9")
        repeat(labels.size) { index ->
            val angle = Math.toRadians((-90f + index * (360f / labels.size)).toDouble())
            val dotRadius = ringWidth * 0.28f
            val dotX = cx + cos(angle).toFloat() * (radius - ringWidth / 2f)
            val dotY = cy + sin(angle).toFloat() * (radius - ringWidth / 2f)
            canvas.drawCircle(dotX, dotY, dotRadius, ringPaint)
        }
    }

    private fun drawPointer(canvas: Canvas, cx: Float, top: Float, radius: Float) {
        val width = radius * 0.3f
        val height = radius * 0.28f
        pointerPath.reset()
        pointerPath.moveTo(cx - width / 2f, top)
        pointerPath.lineTo(cx + width / 2f, top)
        pointerPath.lineTo(cx, top + height)
        pointerPath.close()
        canvas.drawCircle(cx, top + height * 0.55f, width * 0.55f, pointerShadowPaint)
        canvas.drawPath(pointerPath, pointerPaint)
    }

    private fun drawCenter(canvas: Canvas, cx: Float, cy: Float) {
        val radius = dp(34f)
        canvas.drawCircle(cx, cy, radius, centerPaint)
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = dp(2f)
        ringPaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius, ringPaint)
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = dp(22f)
        canvas.drawText("Spin", cx, cy + textPaint.textSize / 3f, textPaint)
        textPaint.color = Color.parseColor("#0F0705")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
