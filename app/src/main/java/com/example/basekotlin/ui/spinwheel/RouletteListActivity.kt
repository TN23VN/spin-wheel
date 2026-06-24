package com.example.spinwheel.ui.spinwheel

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.data.WheelRepository
import com.example.spinwheel.databinding.ActivityRouletteListBinding
import com.example.spinwheel.dialog.common.ConfirmDialog
import com.example.spinwheel.dialog.common.SingleChoiceDialog
import com.example.spinwheel.model.WheelModel

class RouletteListActivity :
    BaseActivity<ActivityRouletteListBinding>(ActivityRouletteListBinding::inflate) {

    private val adapter = WheelListAdapter(
        onOpen = { openSpin(it.id) },
        onEdit = { openEditor(it.id) },
        onDelete = { confirmDelete(it) },
        onDuplicate = { duplicate(it) },
    )

    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.roulette_list)
        binding.viewTop.ivRight.inVisible()
        binding.rvWheels.layoutManager = LinearLayoutManager(this)
        binding.rvWheels.adapter = adapter
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { confirmLeave() }
        binding.btnAdd.tap { addNewRoulette() }
        binding.rlAddNewRou.tap { addNewRoulette() }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val wheels = WheelRepository.getWheels(this)
        adapter.submit(wheels)
        binding.emptyState.visibility = if (wheels.isEmpty()) View.VISIBLE else View.GONE
        binding.btnAdd.visibility = if (wheels.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openSpin(id: Long) {
        startNextActivity(SpinWheelActivity::class.java, Bundle().apply {
            putLong(SpinWheelActivity.EXTRA_WHEEL_ID, id)
        })
    }

    private fun openEditor(id: Long) {
        startNextActivity(WheelEditorActivity::class.java, Bundle().apply {
            putLong(WheelEditorActivity.EXTRA_WHEEL_ID, id)
        })
    }

    private fun addNewRoulette() {
        startNextActivity(WheelEditorActivity::class.java, null)
    }

    private fun showThemePicker() {
        SingleChoiceDialog(
            context = this,
            title = getString(R.string.choose_wheel_color),
            options = WheelRepository.themes,
            selectedIndex = 0,
            confirmText = getString(R.string.next),
            onConfirm = { selected ->
                startNextActivity(WheelEditorActivity::class.java, Bundle().apply {
                    putInt(WheelEditorActivity.EXTRA_THEME_INDEX, selected)
                })
            },
        ).show()
    }

    private fun duplicate(wheel: WheelModel) {
        when (WheelRepository.duplicateWheel(this, wheel.id)) {
            is WheelRepository.DuplicateResult.Success -> {
                Toast.makeText(this, R.string.duplicated, Toast.LENGTH_SHORT).show()
                reload()
            }

            WheelRepository.DuplicateResult.LimitReached -> {
                Toast.makeText(this, R.string.duplicate_limit, Toast.LENGTH_SHORT).show()
            }

            WheelRepository.DuplicateResult.NotFound -> Unit
        }
    }

    private fun confirmDelete(wheel: WheelModel) {
        ConfirmDialog(
            context = this,
            title = getString(R.string.delete_wheel_title),
            message = getString(R.string.delete_wheel_message),
            confirmText = getString(R.string.delete),
            onConfirm = {
                WheelRepository.deleteWheel(this, wheel.id)
                reload()
            },
        ).show()
    }

    private fun confirmLeave() {
        ConfirmDialog(
            context = this,
            title = getString(R.string.warning),
            message = getString(R.string.if_you_leave_now),
            confirmText = getString(R.string.exit),
            onConfirm = {
                onBack()
            },
        ).show()
    }
}
