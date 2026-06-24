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
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MiniWheelPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val colors = intArrayOf(
        Color.parseColor("#FFBA5F"),
        Color.parseColor("#F568B4"),
        Color.parseColor("#FFFF74"),
        Color.parseColor("#CA8AF2"),
        Color.parseColor("#66F766"),
        Color.parseColor("#A6E4EF"),
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = min(width, height) * 0.42f
        val cx = width / 2f
        val cy = height / 2f + dp(8f)
        val inner = radius * 0.82f
        rect.set(cx - inner, cy - inner, cx + inner, cy + inner)
        repeat(6) { index ->
            paint.style = Paint.Style.FILL
            paint.color = colors[index]
            canvas.drawArc(rect, -90f + index * 60f, 60f, true, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.16f
        paint.color = Color.parseColor("#FF6656")
        canvas.drawCircle(cx, cy, radius * 0.9f, paint)
        paint.color = Color.WHITE
        repeat(6) { index ->
            val angle = Math.toRadians((-90f + index * 60f).toDouble())
            canvas.drawCircle(
                cx + cos(angle).toFloat() * radius * 0.9f,
                cy + sin(angle).toFloat() * radius * 0.9f,
                radius * 0.08f,
                paint,
            )
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#C4251B")
        canvas.drawCircle(cx, cy, radius * 0.2f, paint)
        val pointer = Path().apply {
            moveTo(cx - radius * 0.18f, cy - radius * 1.14f)
            lineTo(cx + radius * 0.18f, cy - radius * 1.14f)
            lineTo(cx, cy - radius * 0.68f)
            close()
        }
        paint.color = Color.parseColor("#FF6B59")
        canvas.drawPath(pointer, paint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

class HomograftPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height * 0.48f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(3f)
        paint.color = Color.parseColor("#474E54")
        path.reset()
        path.moveTo(cx - dp(58f), cy)
        path.cubicTo(cx - dp(22f), cy - dp(34f), cx + dp(22f), cy + dp(34f), cx + dp(58f), cy)
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#D9231F")
        canvas.drawRoundRect(cx - dp(84f), cy - dp(10f), cx - dp(48f), cy + dp(18f), dp(4f), dp(4f), paint)
        canvas.drawRoundRect(cx + dp(48f), cy - dp(10f), cx + dp(84f), cy + dp(18f), dp(4f), dp(4f), paint)

        paint.color = Color.parseColor("#FFD7B2")
        canvas.drawCircle(cx - dp(44f), cy, dp(12f), paint)
        canvas.drawCircle(cx + dp(44f), cy, dp(12f), paint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

class ChooseNumberPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height * 0.48f
        paint.textSize = dp(34f)
        paint.color = Color.parseColor("#D9231F")
        canvas.drawText("1 2 3", cx, cy, paint)

        paint.strokeWidth = dp(6f)
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(cx - dp(22f), cy + dp(24f), cx - dp(48f), cy + dp(48f), paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx - dp(50f), cy + dp(50f), dp(7f), paint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

class ChallengeTouchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private var state = State.WAITING
    private var resultCount = 0
    private var totalPlayers = 0
    private var resultText = ""
    private var winnerIndexes = emptySet<Int>()
    private var teamAssignments = emptyList<Int>()
    private var teamAnimationProgress = 1f
    private var teamAnimator: ValueAnimator? = null

    fun reveal() {
        showWinners(1, 1)
    }

    fun showWinners(count: Int, players: Int) {
        teamAnimator?.cancel()
        state = State.WINNERS
        resultCount = count.coerceAtLeast(1)
        totalPlayers = players.coerceAtLeast(resultCount).coerceAtMost(MAX_VISIBLE_PLAYERS)
        winnerIndexes = (0 until totalPlayers).shuffled().take(resultCount).toSet()
        teamAssignments = emptyList()
        teamAnimationProgress = 1f
        resultText = "Winner"
        invalidate()
    }

    fun showTeams(
        count: Int,
        players: Int,
        animationDurationMs: Long = 0L,
        onAnimationEnd: (() -> Unit)? = null,
    ) {
        teamAnimator?.cancel()
        state = State.TEAMS
        resultCount = count.coerceIn(MIN_TEAM_COUNT, MAX_TEAM_COUNT)
        totalPlayers = players.coerceAtLeast(resultCount).coerceAtMost(MAX_VISIBLE_PLAYERS)
        val assignments = mutableListOf<Int>()
        val baseTeamSize = totalPlayers / resultCount
        val remainder = totalPlayers % resultCount
        repeat(resultCount) { team ->
            repeat(baseTeamSize + if (team < remainder) 1 else 0) {
                assignments.add(team)
            }
        }
        teamAssignments = assignments.shuffled()
        winnerIndexes = emptySet()
        teamAnimationProgress = if (animationDurationMs > 0L) 0f else 1f
        resultText = "Team"
        invalidate()

        if (animationDurationMs <= 0L) {
            onAnimationEnd?.invoke()
            return
        }

        startTeamAnimation(animationDurationMs, onAnimationEnd)
    }

    fun reset() {
        teamAnimator?.cancel()
        state = State.WAITING
        resultCount = 0
        totalPlayers = 0
        resultText = ""
        winnerIndexes = emptySet()
        teamAssignments = emptyList()
        teamAnimationProgress = 1f
        invalidate()
    }

    private fun startTeamAnimation(animationDurationMs: Long, onAnimationEnd: (() -> Unit)?) {
        var wasCanceled = false
        teamAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDurationMs
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                teamAnimationProgress = animator.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    wasCanceled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    teamAnimator = null
                    if (!wasCanceled) {
                        teamAnimationProgress = 1f
                        invalidate()
                        onAnimationEnd?.invoke()
                    }
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (state) {
            State.WAITING -> drawFinger(canvas)
            State.WINNERS -> drawWinners(canvas)
            State.TEAMS -> drawTeams(canvas)
        }
    }

    private fun drawFinger(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#E7B990")
        canvas.drawRoundRect(cx - dp(12f), cy - dp(52f), cx + dp(12f), cy + dp(44f), dp(14f), dp(14f), paint)
        paint.color = Color.parseColor("#C98F69")
        canvas.drawRoundRect(cx - dp(8f), cy - dp(72f), cx + dp(8f), cy - dp(34f), dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = Color.parseColor("#9A6045")
        canvas.drawLine(cx - dp(10f), cy + dp(4f), cx + dp(8f), cy - dp(2f), paint)
        canvas.drawLine(cx - dp(9f), cy + dp(18f), cx + dp(8f), cy + dp(12f), paint)
    }

    private fun drawWinners(canvas: Canvas) {
        val colors = intArrayOf(
            Color.parseColor("#D90000"),
            Color.parseColor("#0048D9"),
            Color.parseColor("#F28000"),
            Color.parseColor("#00C425"),
            Color.parseColor("#F568B4"),
        )
        paint.style = Paint.Style.FILL
        repeat(totalPlayers.coerceIn(resultCount, MAX_VISIBLE_PLAYERS)) { index ->
            paint.color = colors[index % colors.size]
            val x = width * (0.18f + index * (0.64f / (totalPlayers.coerceAtLeast(2) - 1)))
            val y = height * (0.32f + (index % 2) * 0.22f)
            canvas.drawCircle(x, y, dp(20f), paint)
            if (index in winnerIndexes) {
                drawCrown(canvas, x, y - dp(30f))
            }
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = dp(16f)
        paint.color = Color.parseColor("#0F0705")
        canvas.drawText(resultText, width / 2f, height * 0.68f, paint)
    }

    private fun drawTeams(canvas: Canvas) {
        val colors = intArrayOf(
            Color.parseColor("#D90000"),
            Color.parseColor("#0048D9"),
            Color.parseColor("#F28000"),
            Color.parseColor("#00C425"),
            Color.parseColor("#F568B4"),
        )
        val players = totalPlayers.coerceIn(resultCount, MAX_VISIBLE_PLAYERS)
        val assignments = teamAssignments.take(players).ifEmpty {
            List(players) { index -> index % resultCount }
        }
        val points = buildPlayerPoints(players)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(4f)
        repeat(resultCount) { team ->
            paint.color = colors[team % colors.size]
            val teamPoints = points.filterIndexed { index, _ -> assignments[index] == team }
            drawTeamConnections(canvas, teamPoints)
        }
        paint.style = Paint.Style.FILL
        points.forEachIndexed { index, point ->
            paint.color = colors[assignments[index] % colors.size]
            canvas.drawCircle(point.first, point.second, dp(22f), paint)
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = dp(16f)
        paint.color = Color.parseColor("#0F0705")
        canvas.drawText("$resultCount Teams", width / 2f, height * 0.72f, paint)
    }

    private fun buildPlayerPoints(players: Int): List<Pair<Float, Float>> {
        if (players <= COMPACT_LAYOUT_PLAYERS) {
            return (0 until players).map { index ->
                val x = width * (0.18f + index * (0.64f / (players.coerceAtLeast(2) - 1)))
                val y = height * (0.34f + (index % 2) * 0.25f)
                x to y
            }
        }

        val cx = width / 2f
        val cy = height * 0.42f
        val radiusX = width * 0.32f
        val radiusY = min(width, height) * 0.24f
        return (0 until players).map { index ->
            val angle = Math.toRadians((-90f + index * (360f / players)).toDouble())
            val x = cx + cos(angle).toFloat() * radiusX
            val y = cy + sin(angle).toFloat() * radiusY
            x to y
        }
    }

    private fun drawTeamConnections(canvas: Canvas, points: List<Pair<Float, Float>>) {
        if (points.size < 2) return

        val segmentProgress = teamAnimationProgress * (points.size - 1)
        points.zipWithNext().forEachIndexed { index, (start, end) ->
            val progress = (segmentProgress - index).coerceIn(0f, 1f)
            if (progress <= 0f) return@forEachIndexed

            val endX = start.first + (end.first - start.first) * progress
            val endY = start.second + (end.second - start.second) * progress
            canvas.drawLine(start.first, start.second, endX, endY, paint)
        }
    }

    private fun drawCrown(canvas: Canvas, cx: Float, cy: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F5B800")
        val crown = Path().apply {
            moveTo(cx - dp(18f), cy + dp(12f))
            lineTo(cx - dp(12f), cy - dp(8f))
            lineTo(cx, cy + dp(2f))
            lineTo(cx + dp(12f), cy - dp(8f))
            lineTo(cx + dp(18f), cy + dp(12f))
            close()
        }
        canvas.drawPath(crown, paint)
    }

    private enum class State {
        WAITING,
        WINNERS,
        TEAMS,
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private companion object {
        private const val MIN_TEAM_COUNT = 2
        private const val MAX_TEAM_COUNT = 5
        private const val MAX_VISIBLE_PLAYERS = 10
        private const val COMPACT_LAYOUT_PLAYERS = 6
    }
}
