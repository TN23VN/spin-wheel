package com.example.spinwheel.ui.homograft

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityHomograftBinding
import com.example.spinwheel.dialog.common.MessageDialog
import com.example.spinwheel.dialog.common.NumberPickerBottomSheet
import com.example.spinwheel.util.SoundPlayer
import java.io.File
import java.io.FileOutputStream

class HomograftActivity : BaseActivity<ActivityHomograftBinding>(ActivityHomograftBinding::inflate) {

    private var teamCount = 2
    private var soundOn = true
    private val soundPlayer by lazy { SoundPlayer(this) }
    private val handler = Handler(Looper.getMainLooper())
    private var gamePhase = GamePhase.WAITING
    private var activePlayers = 0
    private var lastMissingPlayers = 0
    private var lastMissingToastAt = 0L
    private var resultPlayers = 0
    private var resultAssignments = emptyList<Int>()
    private val requiredPlayers: Int
        get() = teamCount

    private val startAnimationRunnable = Runnable {
        if (gamePhase != GamePhase.HOLDING || activePlayers < requiredPlayers) return@Runnable
        gamePhase = GamePhase.ANIMATING
        resultPlayers = activePlayers
        resultAssignments = buildTeamAssignments(teamCount, resultPlayers)
        handler.postDelayed(finishAnimationRunnable, TEAM_ANIMATION_MS)
    }

    private val finishAnimationRunnable = Runnable {
        if (gamePhase == GamePhase.ANIMATING) {
            gamePhase = GamePhase.RESULT
            openResultScreen()
        }
    }

    override fun initView() {
        binding.ivRight.setImageResource(R.drawable.ic_volume_up_black)
        binding.tvTeamCount.text = teamCount.toString()
        val prefs = getSharedPreferences("feature_tutorial", MODE_PRIVATE)
        if (!prefs.getBoolean("homograft_done", false)) {
            MessageDialog(
                context = this,
                title = getString(R.string.homograft),
                message = getString(R.string.homograft_tutorial),
                onOk = {
                    prefs.edit().putBoolean("homograft_done", true).apply()
                },
            ).show()
        }
    }

    override fun bindView() {
        binding.ivLeft.tap { onBack() }
        binding.btnRestart.tap { resetGame() }
        binding.teamChip.tap {
            showTeamCountPicker()
        }
        binding.ivRight.tap {
            soundOn = !soundOn
            binding.ivRight.setImageResource(
                if (soundOn) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            )
            if (soundOn && (gamePhase == GamePhase.HOLDING || gamePhase == GamePhase.ANIMATING)) {
                soundPlayer.play(R.raw.homograft_choose_number, loop = true)
            } else if (!soundOn) {
                soundPlayer.stop()
            }
        }
        binding.teamView.setOnTouchListener { view, event ->
            handleTouch(event)
            view.performClick()
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE -> handleActivePointers(event.pointerCount)

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> handleReleasedPointers(remainingPointerCount(event))
        }
    }

    private fun handleActivePointers(playerCount: Int) {
        if (gamePhase == GamePhase.ANIMATING || gamePhase == GamePhase.RESULT) return

        activePlayers = playerCount
        if (activePlayers >= requiredPlayers) {
            lastMissingPlayers = 0
            if (gamePhase == GamePhase.WAITING) {
                startHoldCountdown()
            }
        } else {
            if (gamePhase == GamePhase.HOLDING) {
                Toast.makeText(this, R.string.need_hold_enough, Toast.LENGTH_SHORT).show()
                cancelHold()
            }
            gamePhase = GamePhase.WAITING
            showMissingPlayersWarning(activePlayers)
        }
    }

    private fun handleReleasedPointers(remainingPlayers: Int) {
        when (gamePhase) {
            GamePhase.WAITING -> {
                activePlayers = remainingPlayers
                if (activePlayers in 1 until requiredPlayers) {
                    showMissingPlayersWarning(activePlayers)
                }
            }

            GamePhase.HOLDING -> {
                activePlayers = remainingPlayers
                if (activePlayers >= requiredPlayers) return

                Toast.makeText(this, R.string.need_hold_enough, Toast.LENGTH_SHORT).show()
                cancelHold()
            }

            GamePhase.ANIMATING -> {
                Toast.makeText(
                    this,
                    if (remainingPlayers == 0) {
                        R.string.need_hold_enough
                    } else {
                        R.string.need_hold_until_animation_done
                    },
                    Toast.LENGTH_SHORT,
                ).show()
                resetGame()
            }

            GamePhase.RESULT -> Unit
        }
    }

    private fun startHoldCountdown() {
        gamePhase = GamePhase.HOLDING
        Toast.makeText(this, R.string.hold_3_seconds, Toast.LENGTH_SHORT).show()
        if (soundOn) {
            soundPlayer.play(R.raw.homograft_choose_number, loop = true)
        }
        handler.postDelayed(startAnimationRunnable, HOLD_DURATION_MS)
    }

    private fun cancelHold() {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        gamePhase = GamePhase.WAITING
        soundPlayer.stop()
    }

    private fun resetGame(stopSound: Boolean = true) {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        gamePhase = GamePhase.WAITING
        activePlayers = 0
        lastMissingPlayers = 0
        resultPlayers = 0
        resultAssignments = emptyList()
        if (stopSound) {
            soundPlayer.stop()
        }
    }

    private fun showTeamCountPicker() {
        NumberPickerBottomSheet(
            context = this,
            title = getString(R.string.number_of_teams),
            options = (2..5).toList(),
            selectedValue = teamCount,
            onSelected = { selectedTeamCount ->
                if (selectedTeamCount != teamCount) {
                    teamCount = selectedTeamCount
                    binding.tvTeamCount.text = teamCount.toString()
                    handleTeamCountChanged()
                }
            },
        ).show()
    }

    private fun handleTeamCountChanged() {
        if (gamePhase == GamePhase.ANIMATING || gamePhase == GamePhase.RESULT) {
            resetGame()
            return
        }

        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        soundPlayer.stop()
        gamePhase = GamePhase.WAITING
        if (activePlayers >= requiredPlayers) {
            startHoldCountdown()
        } else if (activePlayers > 0) {
            showMissingPlayersWarning(activePlayers)
        }
    }

    private fun openResultScreen() {
        val resultFile = captureTeamResult()
        if (resultFile == null) {
            Toast.makeText(this, R.string.please_wait_result, Toast.LENGTH_SHORT).show()
            resetGame()
            return
        }
        soundPlayer.stop()
        if (soundOn) {
            soundPlayer.play(R.raw.gamevictory)
        }
        startNextActivity(
            HomograftResultActivity::class.java,
            Bundle().apply {
                putString(HomograftResultActivity.EXTRA_IMAGE_PATH, resultFile.absolutePath)
            },
        )
        resetGame(stopSound = false)
    }

    private fun captureTeamResult(): File? {
        val width = binding.teamView.width.takeIf { it > 0 } ?: binding.root.width
        val height = binding.teamView.height.takeIf { it > 0 } ?: binding.root.height
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(ContextCompat.getColor(this, R.color.color_app_background))
        drawTeamResult(canvas, width, height)

        val file = File(cacheDir, "homograft_result_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return file
    }

    private fun drawTeamResult(canvas: Canvas, width: Int, height: Int) {
        val players = resultPlayers.coerceAtLeast(teamCount).coerceAtMost(MAX_VISIBLE_RESULT_PLAYERS)
        val assignments = resultAssignments.take(players).ifEmpty {
            buildTeamAssignments(teamCount, players)
        }
        val points = buildPlayerPoints(width, height, players)
        val colors = intArrayOf(
            Color.parseColor("#D90000"),
            Color.parseColor("#0048D9"),
            Color.parseColor("#F28000"),
            Color.parseColor("#00C425"),
            Color.parseColor("#F568B4"),
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            textAlign = Paint.Align.CENTER
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(4f)
        repeat(teamCount) { team ->
            paint.color = colors[team % colors.size]
            points.filterIndexed { index, _ -> assignments[index] == team }
                .zipWithNext()
                .forEach { (start, end) ->
                    canvas.drawLine(start.first, start.second, end.first, end.second, paint)
                }
        }

        paint.style = Paint.Style.FILL
        points.forEachIndexed { index, point ->
            paint.color = colors[assignments[index] % colors.size]
            canvas.drawCircle(point.first, point.second, dp(22f), paint)
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = dp(18f)
        paint.color = ContextCompat.getColor(this, R.color.color_main_text)
        canvas.drawText("$teamCount Teams", width / 2f, height * 0.74f, paint)
    }

    private fun buildTeamAssignments(teamCount: Int, players: Int): List<Int> {
        val safeTeamCount = teamCount.coerceIn(MIN_TEAM_COUNT, MAX_TEAM_COUNT)
        val safePlayers = players.coerceAtLeast(safeTeamCount)
        val baseTeamSize = safePlayers / safeTeamCount
        val remainder = safePlayers % safeTeamCount
        return buildList {
            repeat(safeTeamCount) { team ->
                repeat(baseTeamSize + if (team < remainder) 1 else 0) {
                    add(team)
                }
            }
        }.shuffled()
    }

    private fun buildPlayerPoints(width: Int, height: Int, players: Int): List<Pair<Float, Float>> {
        if (players <= COMPACT_RESULT_PLAYERS) {
            return (0 until players).map { index ->
                val x = width * (0.18f + index * (0.64f / (players.coerceAtLeast(2) - 1)))
                val y = height * (0.34f + (index % 2) * 0.25f)
                x to y
            }
        }

        val cx = width / 2f
        val cy = height * 0.42f
        val radiusX = width * 0.32f
        val radiusY = minOf(width, height) * 0.24f
        return (0 until players).map { index ->
            val angle = Math.toRadians((-90f + index * (360f / players)).toDouble())
            val x = cx + kotlin.math.cos(angle).toFloat() * radiusX
            val y = cy + kotlin.math.sin(angle).toFloat() * radiusY
            x to y
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun remainingPointerCount(event: MotionEvent): Int {
        return when (event.actionMasked) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> 0

            MotionEvent.ACTION_POINTER_UP -> event.pointerCount - 1
            else -> event.pointerCount
        }
    }

    private fun showMissingPlayersWarning(playerCount: Int) {
        val missingPlayers = requiredPlayers - playerCount
        if (missingPlayers <= 0) return

        val now = SystemClock.uptimeMillis()
        if (
            missingPlayers == lastMissingPlayers &&
            now - lastMissingToastAt < MISSING_TOAST_COOLDOWN_MS
        ) {
            return
        }

        lastMissingPlayers = missingPlayers
        lastMissingToastAt = now
        Toast.makeText(
            this,
            getString(R.string.need_more_fingers_count, missingPlayers),
            Toast.LENGTH_SHORT,
        ).show()
    }

    override fun onBack() {
        resetGame()
        super.onBack()
    }

    override fun onDestroy() {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        soundPlayer.release()
        super.onDestroy()
    }

    private enum class GamePhase {
        WAITING,
        HOLDING,
        ANIMATING,
        RESULT,
    }

    private companion object {
        private const val MIN_TEAM_COUNT = 2
        private const val MAX_TEAM_COUNT = 5
        private const val MAX_VISIBLE_RESULT_PLAYERS = 10
        private const val COMPACT_RESULT_PLAYERS = 6
        private const val HOLD_DURATION_MS = 3000L
        private const val TEAM_ANIMATION_MS = 1600L
        private const val MISSING_TOAST_COOLDOWN_MS = 1000L
    }
}
