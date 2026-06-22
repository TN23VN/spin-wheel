package com.example.spinwheel.ui.spinwheel

import android.os.Bundle
import android.widget.Toast
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.data.WheelRepository
import com.example.spinwheel.databinding.ActivitySpinWheelBinding
import com.example.spinwheel.dialog.winner.WinnerResultDialog
import com.example.spinwheel.model.WheelModel
import com.example.spinwheel.model.WheelSlice

class SpinWheelActivity : BaseActivity<ActivitySpinWheelBinding>(ActivitySpinWheelBinding::inflate) {

    private var soundOn = true
    private var isSpinning = false
    private var wheelId: Long = -1L
    private lateinit var wheel: WheelModel

    override fun getData() {
        wheelId = intent.getLongExtra(EXTRA_WHEEL_ID, -1L)
    }

    override fun initView() {
        binding.viewTop.ivRight.setImageResource(R.drawable.ic_volume_up_black)
        loadWheel()
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }
        binding.spinWheel.tap { spin() }
        binding.btnEdit.tap { openEditor() }
        binding.btnMoreOptions.tap {
            startNextActivity(RouletteListActivity::class.java, null)
        }
        binding.viewTop.ivRight.tap {
            soundOn = !soundOn
            binding.viewTop.ivRight.setImageResource(
                if (soundOn) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::wheel.isInitialized) {
            loadWheel()
        }
    }

    private fun loadWheel() {
        wheel = if (wheelId != -1L) {
            WheelRepository.getWheel(this, wheelId) ?: WheelRepository.getDefaultWheel(this)
        } else {
            WheelRepository.getDefaultWheel(this)
        }
        wheelId = wheel.id
        binding.viewTop.tvToolBar.text = wheel.name.ifBlank { getString(R.string.spin_the_wheel) }
        binding.tvQuestion.text = wheel.name.ifBlank { getString(R.string.wheel_question) }
        updateWheelView()
    }

    private fun updateWheelView() {
        val repeated = mutableListOf<WheelSlice>()
        repeat(wheel.repeat.coerceAtLeast(1)) {
            repeated.addAll(wheel.slices.map { it.copy() })
        }
        binding.spinWheel.setSlices(repeated, wheel.fontSize)
    }

    private fun spin() {
        if (isSpinning) {
            Toast.makeText(this, R.string.please_wait_result, Toast.LENGTH_SHORT).show()
            return
        }

        val active = wheel.slices.filterNot { it.hidden }
        if (active.size < 2) {
            Toast.makeText(this, R.string.add_at_least_two_slices, Toast.LENGTH_SHORT).show()
            return
        }

        isSpinning = true
        binding.spinWheel.spinToRandom { winner ->
            isSpinning = false
            showResult(winner)
        }
    }

    private fun showResult(winner: String) {
        WinnerResultDialog(
            context = this,
            winner = winner,
            onRemove = { hideWinner(winner) },
        ).show()
    }

    private fun hideWinner(winner: String) {
        val active = wheel.slices.filterNot { it.hidden }
        if (active.size <= 2) {
            Toast.makeText(this, R.string.keep_at_least_two_slices, Toast.LENGTH_SHORT).show()
            return
        }
        wheel.slices.firstOrNull { !it.hidden && it.label.ifBlank { " " } == winner }?.hidden = true
        WheelRepository.saveWheel(this, wheel)
        updateWheelView()
    }

    private fun openEditor() {
        startNextActivity(WheelEditorActivity::class.java, Bundle().apply {
            putLong(WheelEditorActivity.EXTRA_WHEEL_ID, wheel.id)
        })
    }

    companion object {
        const val EXTRA_WHEEL_ID = "extra_wheel_id"
    }
}
