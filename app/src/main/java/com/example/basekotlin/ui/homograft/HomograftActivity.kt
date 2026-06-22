package com.example.spinwheel.ui.homograft

import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityHomograftBinding
import com.example.spinwheel.dialog.common.MessageDialog

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
        binding.viewTop.tvToolBar.text = getString(R.string.homograft)
        binding.viewTop.ivRight.setImageResource(R.drawable.ic_volume_up_black)
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
        binding.viewTop.ivLeft.tap { onBack() }
        binding.btnRestart.tap { resetGame() }
        binding.teamChip.tap {
            teamCount = if (teamCount >= 5) 2 else teamCount + 1
            binding.tvTeamCount.text = teamCount.toString()
            resetGame()
        }
        binding.viewTop.ivRight.tap {
            soundOn = !soundOn
            binding.viewTop.ivRight.setImageResource(
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
