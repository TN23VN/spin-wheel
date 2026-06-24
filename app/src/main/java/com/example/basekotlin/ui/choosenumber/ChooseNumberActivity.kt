package com.example.spinwheel.ui.choosenumber

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.Toast
import com.example.spinwheel.DareFragment
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityChooseNumberBinding
import com.example.spinwheel.dialog.common.MessageDialog
import com.example.spinwheel.dialog.common.NumberPickerBottomSheet
import com.example.spinwheel.util.SoundPlayer

class ChooseNumberActivity :
    BaseActivity<ActivityChooseNumberBinding>(ActivityChooseNumberBinding::inflate) {

    private var count = 1
    private var soundOn = true
    private val soundPlayer by lazy { SoundPlayer(this) }
    private val handler = Handler(Looper.getMainLooper())
    private val activeTouchPoints = linkedMapOf<Int, PointF>()
    private var gamePhase = GamePhase.WAITING
    private var activePlayers = 0
    private var resultPlayers = 0
    private var resultWinnerCount = 0
    private var lastMissingPlayers = 0
    private var lastMissingToastAt = 0L
    private val requiredPlayers: Int
        get() = count + 1

    private val startAnimationRunnable = Runnable {
        if (gamePhase != GamePhase.HOLDING || activePlayers < requiredPlayers) return@Runnable
        startWinnerAnimation()
    }

    private val finishAnimationRunnable = Runnable {
        if (gamePhase == GamePhase.ANIMATING) {
            finishWinnerAnimation()
        }
    }

    private val openDareRunnable = Runnable {
        showDareScreen()
    }

    override fun initView() {
        binding.ivRight.setImageResource(R.drawable.ic_volume_up_black)
        binding.tvNumber.text = count.toString()
        binding.winnerView.reset()

        val prefs = getSharedPreferences("feature_tutorial", MODE_PRIVATE)
        setChallengeViewVisible(!prefs.getBoolean(CHALLENGE_VIEW_DONE_KEY, false))
        if (!prefs.getBoolean(CHOOSE_NUMBER_TUTORIAL_DONE_KEY, false)) {
            MessageDialog(
                context = this,
                title = getString(R.string.choose_number),
                message = getString(R.string.choose_number_tutorial),
                onOk = {
                    prefs.edit().putBoolean(CHOOSE_NUMBER_TUTORIAL_DONE_KEY, true).apply()
                },
            ).show()
        }
    }

    override fun bindView() {
        binding.ivLeft.tap { onBack() }
        binding.btnRestart.tap { resetGame() }
        binding.numberChip.tap {
            showNumberPicker()
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
        binding.challengeView.setOnTouchListener { view, event ->
            handleTouch(event)
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                view.performClick()
            }
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        val playersBeforeEvent = activePlayers
        syncActiveTouches(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE -> handleActivePointers(activePlayers)

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> handleReleasedPointers(activePlayers, playersBeforeEvent)
        }
    }

    private fun handleActivePointers(playerCount: Int) {
        if (gamePhase == GamePhase.ANIMATING || gamePhase == GamePhase.RESULT) return

        if (playerCount >= requiredPlayers) {
            activePlayers = playerCount
            lastMissingPlayers = 0
            if (gamePhase == GamePhase.WAITING) {
                startHoldCountdown()
            }
        } else {
            activePlayers = playerCount
            if (gamePhase == GamePhase.HOLDING) {
                Toast.makeText(this, R.string.need_hold_enough, Toast.LENGTH_SHORT).show()
                cancelHold()
            }
            gamePhase = GamePhase.WAITING
            showMissingPlayersWarning(playerCount)
        }
    }

    private fun handleReleasedPointers(remainingPlayers: Int, playersBeforeEvent: Int) {
        when (gamePhase) {
            GamePhase.WAITING -> {
                activePlayers = remainingPlayers
                if (remainingPlayers in 1 until requiredPlayers) {
                    showMissingPlayersWarning(remainingPlayers)
                } else if (remainingPlayers == 0 && playersBeforeEvent in 1 until requiredPlayers) {
                    Toast.makeText(this, R.string.need_more_players, Toast.LENGTH_SHORT).show()
                }
            }

            GamePhase.HOLDING -> {
                activePlayers = remainingPlayers
                if (remainingPlayers >= requiredPlayers) return

                Toast.makeText(this, R.string.need_hold_enough, Toast.LENGTH_SHORT).show()
                cancelHold()
            }

            GamePhase.ANIMATING -> {
                if (remainingPlayers >= resultPlayers) return

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
        handler.removeCallbacks(startAnimationRunnable)
        gamePhase = GamePhase.HOLDING
        Toast.makeText(this, R.string.hold_3_seconds, Toast.LENGTH_SHORT).show()
        if (soundOn) {
            soundPlayer.play(R.raw.homograft_choose_number, loop = true)
        }
        handler.postDelayed(startAnimationRunnable, HOLD_DURATION_MS)
    }

    private fun startWinnerAnimation() {
        gamePhase = GamePhase.ANIMATING
        resultPlayers = activePlayers
        resultWinnerCount = count
        binding.challengeView.alpha = 0f
        binding.winnerView.showWinners(
            winnerCount = resultWinnerCount,
            touchPoints = activeTouchPoints.values.map { PointF(it.x, it.y) },
            durationMs = WINNER_ANIMATION_MS,
        )
        handler.postDelayed(finishAnimationRunnable, WINNER_ANIMATION_MS)
    }

    private fun finishWinnerAnimation() {
        gamePhase = GamePhase.RESULT
        soundPlayer.stop()
        if (soundOn) {
            soundPlayer.play(R.raw.gamevictory)
        }
        handler.postDelayed(openDareRunnable, RESULT_TO_DARE_DELAY_MS)
    }

    private fun cancelHold() {
        handler.removeCallbacks(startAnimationRunnable)
        gamePhase = GamePhase.WAITING
        soundPlayer.stop()
    }

    private fun resetGame(stopSound: Boolean = true) {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        handler.removeCallbacks(openDareRunnable)
        gamePhase = GamePhase.WAITING
        activeTouchPoints.clear()
        activePlayers = 0
        resultPlayers = 0
        resultWinnerCount = 0
        lastMissingPlayers = 0
        binding.winnerView.reset()
        setChallengeViewVisible(shouldShowChallengeView())
        if (stopSound) {
            soundPlayer.stop()
        }
    }

    private fun showNumberPicker() {
        NumberPickerBottomSheet(
            context = this,
            title = getString(R.string.number_of_chosen_people),
            options = (1..4).toList(),
            selectedValue = count,
            onSelected = { selectedCount ->
                if (selectedCount != count) {
                    count = selectedCount
                    binding.tvNumber.text = count.toString()
                    handleWinnerCountChanged()
                }
            },
        ).show()
    }

    private fun handleWinnerCountChanged() {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        handler.removeCallbacks(openDareRunnable)
        binding.winnerView.reset()
        soundPlayer.stop()
        resultPlayers = 0
        resultWinnerCount = 0
        lastMissingPlayers = 0
        gamePhase = GamePhase.WAITING
        setChallengeViewVisible(shouldShowChallengeView())

        if (activePlayers >= requiredPlayers) {
            startHoldCountdown()
        } else if (activePlayers > 0) {
            showMissingPlayersWarning(activePlayers)
        }
    }

    private fun showDareScreen() {
        if (supportFragmentManager.findFragmentByTag(DARE_FRAGMENT_TAG) != null) return
        val selectedCount = resultWinnerCount.takeIf { it > 0 } ?: count
        markChallengeViewOpened()
        resetGame(stopSound = false)
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, DareFragment.newInstance(selectedCount), DARE_FRAGMENT_TAG)
            .addToBackStack(DARE_FRAGMENT_TAG)
            .commit()
    }

    override fun onBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            resetGame()
            return
        }
        resetGame()
        super.onBack()
    }

    override fun onDestroy() {
        handler.removeCallbacks(startAnimationRunnable)
        handler.removeCallbacks(finishAnimationRunnable)
        handler.removeCallbacks(openDareRunnable)
        binding.winnerView.reset()
        soundPlayer.release()
        super.onDestroy()
    }

    private fun syncActiveTouches(event: MotionEvent) {
        activeTouchPoints.clear()
        when (event.actionMasked) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                activePlayers = 0
                return
            }

            MotionEvent.ACTION_POINTER_UP -> {
                repeat(event.pointerCount) { index ->
                    if (index != event.actionIndex) {
                        activeTouchPoints[event.getPointerId(index)] =
                            PointF(event.getX(index), event.getY(index))
                    }
                }
            }

            else -> {
                repeat(event.pointerCount) { index ->
                    activeTouchPoints[event.getPointerId(index)] =
                        PointF(event.getX(index), event.getY(index))
                }
            }
        }
        activePlayers = activeTouchPoints.size
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

    private fun markChallengeViewOpened() {
        getSharedPreferences("feature_tutorial", MODE_PRIVATE)
            .edit()
            .putBoolean(CHALLENGE_VIEW_DONE_KEY, true)
            .apply()
        hideChallengeView()
    }

    private fun hideChallengeView() {
        setChallengeViewVisible(false)
    }

    private fun shouldShowChallengeView(): Boolean {
        return !getSharedPreferences("feature_tutorial", MODE_PRIVATE)
            .getBoolean(CHALLENGE_VIEW_DONE_KEY, false)
    }

    private fun setChallengeViewVisible(visible: Boolean) {
        binding.challengeView.alpha = if (visible) 1f else 0f
    }

    private enum class GamePhase {
        WAITING,
        HOLDING,
        ANIMATING,
        RESULT,
    }

    private companion object {
        private const val DARE_FRAGMENT_TAG = "dare_fragment"
        private const val CHOOSE_NUMBER_TUTORIAL_DONE_KEY = "choose_number_done"
        private const val CHALLENGE_VIEW_DONE_KEY = "choose_number_challenge_view_done"
        private const val HOLD_DURATION_MS = 3000L
        private const val WINNER_ANIMATION_MS = 1600L
        private const val RESULT_TO_DARE_DELAY_MS = 600L
        private const val MISSING_TOAST_COOLDOWN_MS = 1000L
    }
}
