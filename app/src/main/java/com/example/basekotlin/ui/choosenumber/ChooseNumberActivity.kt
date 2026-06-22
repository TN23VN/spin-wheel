package com.example.spinwheel.ui.choosenumber

import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityChooseNumberBinding
import com.example.spinwheel.dialog.choosenumber.DareDialog
import com.example.spinwheel.dialog.common.MessageDialog

class ChooseNumberActivity :
    BaseActivity<ActivityChooseNumberBinding>(ActivityChooseNumberBinding::inflate) {

    private var count = 2
    private var soundOn = true
    private val handler = Handler()
    private var holdStarted = false
    private var resultShown = false
    private var activePlayers = 0
    private val revealRunnable = Runnable {
        resultShown = true
        binding.challengeView.showWinners(count, activePlayers)
        binding.challengeView.postDelayed({ showDareDialog() }, 600)
    }

    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.choose_number)
        binding.viewTop.ivRight.setImageResource(R.drawable.ic_volume_up_black)
        val prefs = getSharedPreferences("feature_tutorial", MODE_PRIVATE)
        if (!prefs.getBoolean("choose_number_done", false)) {
            MessageDialog(
                context = this,
                title = getString(R.string.choose_number),
                message = getString(R.string.choose_number_tutorial),
                onOk = {
                    prefs.edit().putBoolean("choose_number_done", true).apply()
                },
            ).show()
        }
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }
        binding.btnRestart.tap { resetGame() }
        binding.numberChip.tap {
            count = if (count >= 4) 1 else count + 1
            binding.tvNumber.text = count.toString()
            resetGame()
        }
        binding.viewTop.ivRight.tap {
            soundOn = !soundOn
            binding.viewTop.ivRight.setImageResource(
                if (soundOn) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            )
        }
        binding.challengeView.setOnTouchListener { view, event ->
            handleTouch(event)
            view.performClick()
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE -> {
                if (resultShown) return
                activePlayers = event.pointerCount
                if (activePlayers >= count) {
                    if (!holdStarted) {
                        holdStarted = true
                        Toast.makeText(this, R.string.hold_3_seconds, Toast.LENGTH_SHORT).show()
                        handler.postDelayed(revealRunnable, 3000)
                    }
                } else {
                    cancelHold()
                }
            }

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (holdStarted && !resultShown) {
                    Toast.makeText(this, R.string.need_hold_enough, Toast.LENGTH_SHORT).show()
                } else if (!resultShown && event.pointerCount < count) {
                    Toast.makeText(this, R.string.need_more_players, Toast.LENGTH_SHORT).show()
                }
                cancelHold()
            }
        }
    }

    private fun cancelHold() {
        holdStarted = false
        handler.removeCallbacks(revealRunnable)
    }

    private fun resetGame() {
        cancelHold()
        resultShown = false
        activePlayers = 0
        binding.challengeView.reset()
    }

    private fun showDareDialog() {
        val options = listOf(
            "Make a lovey-dovey face for 5 seconds",
            "Raise your hand and shout: I am the chosen one",
            "Do a belly dance for 5 minutes straight",
            "Call your mom and say you were arrested",
            "Drink some Tequila",
            "Eat some raw eggs",
            "Sing a chorus loudly",
            "Tell one funny secret",
            "Do ten squats",
            "Speak with an accent for one minute",
            "Let the group choose a nickname",
            "Say something nice to every player",
            "Pose like a superstar",
            "Read the last message you sent",
            "Let someone pick your next challenge",
        )
        DareDialog(
            context = this,
            options = options,
            selectionLimit = count,
            onRestart = { resetGame() },
        ).show()
    }
}
