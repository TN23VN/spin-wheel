package com.example.spinwheel.ui.homograft

import android.app.AlertDialog
import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityHomograftBinding

class HomograftActivity : BaseActivity<ActivityHomograftBinding>(ActivityHomograftBinding::inflate) {

    private var teamCount = 2
    private var soundOn = true
    private val handler = Handler()
    private var holdStarted = false
    private var resultShown = false
    private var activePlayers = 0
    private val revealRunnable = Runnable {
        resultShown = true
        binding.teamView.showTeams(teamCount, activePlayers)
    }

    override fun initView() {
        val prefs = getSharedPreferences("feature_tutorial", MODE_PRIVATE)
        if (!prefs.getBoolean("homograft_done", false)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.homograft)
                .setMessage(R.string.homograft_tutorial)
                .setPositiveButton(R.string.ok) { _, _ ->
                    prefs.edit().putBoolean("homograft_done", true).apply()
                }
                .show()
        }
    }

    override fun bindView() {
        binding.btnBack.tap { onBack() }
        binding.btnRestart.tap { resetGame() }
        binding.teamChip.tap {
            teamCount = if (teamCount >= 5) 2 else teamCount + 1
            binding.tvTeamCount.text = teamCount.toString()
            resetGame()
        }
        binding.btnSound.tap {
            soundOn = !soundOn
            binding.btnSound.setImageResource(
                if (soundOn) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            )
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
            MotionEvent.ACTION_MOVE -> {
                if (resultShown) return
                activePlayers = event.pointerCount
                if (activePlayers >= teamCount) {
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
                } else if (!resultShown && event.pointerCount < teamCount) {
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
        binding.teamView.reset()
    }
}
