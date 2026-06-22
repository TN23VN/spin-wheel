package com.example.spinwheel.ui.spinwheel

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.data.WheelRepository
import com.example.spinwheel.databinding.ActivityRouletteListBinding
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
        binding.rvWheels.layoutManager = LinearLayoutManager(this)
        binding.rvWheels.adapter = adapter
    }

    override fun bindView() {
        binding.btnBack.tap { onBack() }
        binding.btnAdd.tap { showThemePicker() }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val wheels = WheelRepository.getWheels(this)
        adapter.submit(wheels)
        binding.emptyState.visibility = if (wheels.isEmpty()) View.VISIBLE else View.GONE
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

    private fun showThemePicker() {
        var selected = 0
        AlertDialog.Builder(this)
            .setTitle(R.string.choose_wheel_color)
            .setSingleChoiceItems(WheelRepository.themes.toTypedArray(), selected) { _, which ->
                selected = which
            }
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.next) { _, _ ->
                startNextActivity(WheelEditorActivity::class.java, Bundle().apply {
                    putInt(WheelEditorActivity.EXTRA_THEME_INDEX, selected)
                })
            }
            .show()
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
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_wheel_title)
            .setMessage(R.string.delete_wheel_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                WheelRepository.deleteWheel(this, wheel.id)
                reload()
            }
            .show()
    }
}
