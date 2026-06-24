package com.example.spinwheel.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import com.example.spinwheel.R
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

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B59")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        strokeWidth = FRAME_STRIPE_WIDTH
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(61, 34, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB4A9")
        style = Paint.Style.FILL
    }
    private val dotShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(61, 163, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 2.6f
        strokeCap = Paint.Cap.ROUND
    }
    private val dotHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(163, 255, 235, 232)
        style = Paint.Style.STROKE
        strokeWidth = 2.6f
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F0705")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B59")
        style = Paint.Style.FILL
    }
    private val pointerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val pointerHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(61, 255, 232, 232)
        style = Paint.Style.FILL
    }
    private val pointerLowlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(31, 18, 9, 2)
        style = Paint.Style.FILL
    }
    private val centerButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C4251B")
        style = Paint.Style.FILL
    }
    private val centerButtonStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEAEC")
        style = Paint.Style.STROKE
        strokeWidth = CENTER_BUTTON_STROKE
    }
    private val centerButtonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val sliceRect = RectF()
    private val ringPath = Path()
    private val pointerPath = Path()
    private val pointerHighlightPath = Path()
    private val pointerLowlightPath = Path()
    private val dotRect = RectF()
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
    private var spinText: CharSequence = context.getString(R.string.spin)

    init {
        buildPointerPaths()
    }

    fun setSlices(items: List<WheelSlice>, fontSize: Int = 14) {
        val visible = items.filterNot { it.hidden }
        labels = visible.map { it.label.ifBlank { " " } }
        colors = visible.mapIndexed { index, slice ->
            if (slice.color == 0) defaultColors[index % defaultColors.size] else slice.color
        }
        fontSizeSp = fontSize.coerceIn(10, 24).toFloat()
        invalidate()
    }

    fun setSpinText(text: CharSequence) {
        spinText = text
        invalidate()
    }

    fun spinToRandom(onFinished: ((String) -> Unit)? = null) {
        if (labels.isEmpty()) return

        animator?.cancel()
        val targetIndex = Random.nextInt(labels.size)
        val winnerLabel = labels[targetIndex]
        val sweep = 360f / labels.size
        val targetRotation = normalizeDegrees(-(targetIndex * sweep + sweep / 2f))
        val clockwiseDelta = normalizeDegrees(targetRotation - normalizeDegrees(wheelRotation))
        val fullSpins = MIN_SPIN_ROUNDS + Random.nextInt(EXTRA_SPIN_ROUNDS + 1)
        val endRotation = wheelRotation + fullSpins * FULL_CIRCLE + clockwiseDelta
        var wasCanceled = false

        setLayerType(LAYER_TYPE_HARDWARE, null)
        animator = ValueAnimator.ofFloat(wheelRotation, endRotation).apply {
            duration = SPIN_DURATION_MS
            interpolator = DecelerateInterpolator(SPIN_DECELERATION_FACTOR)
            addUpdateListener {
                wheelRotation = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    wasCanceled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    animator = null
                    setLayerType(LAYER_TYPE_NONE, null)
                    wheelRotation = normalizeDegrees(wheelRotation)
                    postInvalidateOnAnimation()
                    if (!wasCanceled) {
                        onFinished?.invoke(winnerLabel)
                    }
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val contentWidth = (width - paddingLeft - paddingRight).toFloat()
        val contentHeight = (height - paddingTop - paddingBottom).toFloat()
        if (contentWidth <= 0f || contentHeight <= 0f) return

        val scale = min(contentWidth / DESIGN_WIDTH, contentHeight / DESIGN_HEIGHT)
        val left = paddingLeft + (contentWidth - DESIGN_WIDTH * scale) / 2f
        val top = paddingTop + (contentHeight - DESIGN_HEIGHT * scale) / 2f

        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)

        canvas.save()
        canvas.rotate(wheelRotation, WHEEL_CX, WHEEL_CY)
        drawSlices(canvas)
        canvas.restore()

        drawFrame(canvas)
        drawCenterButton(canvas)
        drawPointer(canvas)
        canvas.restore()
    }

    private fun drawSlices(canvas: Canvas) {
        if (labels.isEmpty()) return

        val sweep = 360f / labels.size
        sliceRect.set(
            WHEEL_CX - INNER_RADIUS,
            WHEEL_CY - INNER_RADIUS,
            WHEEL_CX + INNER_RADIUS,
            WHEEL_CY + INNER_RADIUS,
        )
        labels.forEachIndexed { index, label ->
            val start = SLICE_START_ANGLE + index * sweep
            slicePaint.color = colors[index % colors.size]
            canvas.drawArc(sliceRect, start, sweep, true, slicePaint)

            val mid = start + sweep / 2f
            val radians = Math.toRadians(mid.toDouble())
            val tx = WHEEL_CX + cos(radians).toFloat() * INNER_RADIUS * 0.58f
            val ty = WHEEL_CY + sin(radians).toFloat() * INNER_RADIUS * 0.58f
            textPaint.textSize = fontSizeSp
            canvas.save()
            canvas.rotate(mid + 90f, tx, ty)
            canvas.drawText(label, tx, ty + textPaint.textSize / 3f, textPaint)
            canvas.restore()
        }
    }

    private fun drawFrame(canvas: Canvas) {
        ringPath.reset()
        ringPath.fillType = Path.FillType.EVEN_ODD
        ringPath.addCircle(WHEEL_CX, WHEEL_CY, OUTER_RADIUS, Path.Direction.CW)
        ringPath.addCircle(WHEEL_CX, WHEEL_CY, INNER_RADIUS, Path.Direction.CW)

        framePaint.style = Paint.Style.FILL
        framePaint.color = Color.WHITE
        canvas.drawPath(ringPath, framePaint)

        canvas.save()
        canvas.clipPath(ringPath)
        drawStripe(canvas, 0f)
        drawStripe(canvas, 45f)
        drawStripe(canvas, 90f)
        drawStripe(canvas, -45f)
        canvas.restore()

        canvas.drawCircle(WHEEL_CX, WHEEL_CY, OUTER_RADIUS, borderPaint)
        canvas.drawCircle(WHEEL_CX, WHEEL_CY, INNER_RADIUS, borderPaint)
        drawFrameDots(canvas)
    }

    private fun drawStripe(canvas: Canvas, angle: Float) {
        val radians = Math.toRadians(angle.toDouble())
        val dx = cos(radians).toFloat() * STRIPE_HALF_LENGTH
        val dy = sin(radians).toFloat() * STRIPE_HALF_LENGTH
        canvas.drawLine(WHEEL_CX - dx, WHEEL_CY - dy, WHEEL_CX + dx, WHEEL_CY + dy, stripePaint)
    }

    private fun drawFrameDots(canvas: Canvas) {
        repeat(DOT_COUNT) { index ->
            val angle = -90f + index * 45f
            val radians = Math.toRadians(angle.toDouble())
            val x = WHEEL_CX + cos(radians).toFloat() * DOT_CENTER_RADIUS
            val y = WHEEL_CY + sin(radians).toFloat() * DOT_CENTER_RADIUS
            canvas.drawCircle(x, y, DOT_RADIUS, dotPaint)

            dotRect.set(x - DOT_RADIUS, y - DOT_RADIUS, x + DOT_RADIUS, y + DOT_RADIUS)
            canvas.drawArc(dotRect, 12f, 98f, false, dotShadowPaint)
            canvas.drawArc(dotRect, 192f, 98f, false, dotHighlightPaint)
        }
    }

    private fun drawCenterButton(canvas: Canvas) {
        canvas.drawCircle(WHEEL_CX, WHEEL_CY, CENTER_BUTTON_RADIUS, centerButtonPaint)
        canvas.drawCircle(WHEEL_CX, WHEEL_CY, CENTER_BUTTON_RADIUS, centerButtonStrokePaint)

        val text = spinText.toString()
        centerButtonTextPaint.textSize = CENTER_TEXT_SIZE
        val maxTextWidth = CENTER_BUTTON_RADIUS * 1.56f
        val textWidth = centerButtonTextPaint.measureText(text)
        if (textWidth > maxTextWidth) {
            centerButtonTextPaint.textSize = CENTER_TEXT_SIZE * (maxTextWidth / textWidth)
        }
        val fontMetrics = centerButtonTextPaint.fontMetrics
        val baseline = WHEEL_CY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, WHEEL_CX, baseline, centerButtonTextPaint)
    }

    private fun drawPointer(canvas: Canvas) {
        drawPointerShadow(canvas, 14f, 15f, Color.argb(5, 0, 0, 0))
        drawPointerShadow(canvas, 8f, 9f, Color.argb(20, 0, 0, 0))
        drawPointerShadow(canvas, 3f, 4f, Color.argb(36, 0, 0, 0))
        drawPointerShadow(canvas, 1f, 1f, Color.argb(41, 0, 0, 0))
        canvas.drawPath(pointerPath, pointerPaint)
        canvas.drawPath(pointerHighlightPath, pointerHighlightPaint)
        canvas.drawPath(pointerLowlightPath, pointerLowlightPaint)
    }

    private fun drawPointerShadow(canvas: Canvas, dx: Float, dy: Float, color: Int) {
        pointerShadowPaint.color = color
        canvas.save()
        canvas.translate(dx, dy)
        canvas.drawPath(pointerPath, pointerShadowPaint)
        canvas.restore()
    }

    private fun buildPointerPaths() {
        pointerPath.reset()
        pointerPath.moveTo(184.609f, 69.5729f)
        pointerPath.cubicTo(186.62f, 74.6017f, 193.739f, 74.6017f, 195.751f, 69.5729f)
        pointerPath.lineTo(218.888f, 11.7283f)
        pointerPath.cubicTo(220.465f, 7.78716f, 217.562f, 3.5f, 213.318f, 3.5f)
        pointerPath.lineTo(167.042f, 3.5f)
        pointerPath.cubicTo(162.797f, 3.5f, 159.895f, 7.78716f, 161.471f, 11.7283f)
        pointerPath.close()

        pointerHighlightPath.reset()
        pointerHighlightPath.moveTo(192.208f, 8.5f)
        pointerHighlightPath.lineTo(168.208f, 8.5f)
        pointerHighlightPath.cubicTo(168.208f, 8.5f, 166.35f, 9.42672f, 165.708f, 10.5f)
        pointerHighlightPath.cubicTo(165f, 11.6852f, 165.208f, 14f, 165.208f, 14f)
        pointerHighlightPath.lineTo(190.208f, 69.5f)
        pointerHighlightPath.lineTo(169.708f, 12f)
        pointerHighlightPath.close()

        pointerLowlightPath.reset()
        pointerLowlightPath.moveTo(188.18f, 68.5f)
        pointerLowlightPath.lineTo(199.767f, 36.0002f)
        pointerLowlightPath.cubicTo(199.767f, 36.0002f, 203.625f, 36.927f, 204.267f, 38.0003f)
        pointerLowlightPath.close()
    }

    private fun normalizeDegrees(value: Float): Float {
        val normalized = value % FULL_CIRCLE
        return if (normalized < 0f) normalized + FULL_CIRCLE else normalized
    }

    companion object {
        private const val FULL_CIRCLE = 360f
        private const val SLICE_START_ANGLE = -90f
        private const val MIN_SPIN_ROUNDS = 5
        private const val EXTRA_SPIN_ROUNDS = 1
        private const val SPIN_DURATION_MS = 2600L
        private const val SPIN_DECELERATION_FACTOR = 1.35f
        private const val DESIGN_WIDTH = 381f
        private const val DESIGN_HEIGHT = 412f
        private const val WHEEL_CX = 190.18f
        private const val WHEEL_CY = 221.5f
        private const val OUTER_RADIUS = 190f
        private const val INNER_RADIUS = 167.2f
        private const val FRAME_STRIPE_WIDTH = 84.44f
        private const val STRIPE_HALF_LENGTH = 270f
        private const val DOT_COUNT = 8
        private const val DOT_CENTER_RADIUS = 177.64f
        private const val DOT_RADIUS = 8.36f
        private const val CENTER_BUTTON_RADIUS = 35f
        private const val CENTER_BUTTON_STROKE = 2f
        private const val CENTER_TEXT_SIZE = 22f
    }
}
