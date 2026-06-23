package com.example.spinwheel.ui.spinwheel

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.data.WheelRepository
import com.example.spinwheel.databinding.ActivityWheelEditorBinding
import com.example.spinwheel.databinding.ItemSliceBinding
import com.example.spinwheel.dialog.common.ColorPickerDialog
import com.example.spinwheel.dialog.common.ConfirmDialog
import com.example.spinwheel.model.WheelModel
import com.example.spinwheel.model.WheelSlice

class WheelEditorActivity :
    BaseActivity<ActivityWheelEditorBinding>(ActivityWheelEditorBinding::inflate) {

    private lateinit var wheel: WheelModel
    private var originalJson = ""
    private var hasChanges = false

    override fun getData() {
        val id = intent.getLongExtra(EXTRA_WHEEL_ID, -1L)
        wheel = if (id != -1L) {
            WheelRepository.getWheel(this, id) ?: WheelRepository.getDefaultWheel(this)
        } else {
            WheelRepository.newWheel(intent.getIntExtra(EXTRA_THEME_INDEX, 0))
        }
        originalJson = wheel.toString()
    }

    override fun initView() {
        binding.viewTop.tvToolBar.text = if (intent.hasExtra(EXTRA_WHEEL_ID)) {
            getString(R.string.edit_wheel)
        } else {
            getString(R.string.new_wheel)
        }
        binding.viewTop.ivRight.inVisible()
        binding.edtName.setText(wheel.name)
        renderSlices()
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { handleBack() }
        binding.btnSave.tap { save() }
        binding.btnPreview.tap { previewWheel() }
        binding.btnEditName.tap { binding.edtName.requestFocus() }

        binding.edtName.addTextChangedListener(simpleWatcher {
            wheel.name = binding.edtName.text.toString()
            markChanged()
        })
    }

    private fun addOption(label: String) {
        if (wheel.slices.size >= MAX_OPTIONS) {
            Toast.makeText(this, R.string.max_24_options, Toast.LENGTH_SHORT).show()
            return
        }

        wheel.slices.add(
            WheelSlice(
                label = label.trim(),
                color = WheelRepository.colorFor(wheel.themeIndex, wheel.slices.size),
            ),
        )
        markChanged()
        renderSlices()
    }

    private fun save() {
        if (!prepareWheel()) return

        persistWheel()
        startNextActivity(SpinWheelActivity::class.java, Bundle().apply {
            putLong(SpinWheelActivity.EXTRA_WHEEL_ID, wheel.id)
        })
        finish()
    }

    private fun previewWheel() {
        if (!prepareWheel()) return

        persistWheel()
        startNextActivity(SpinWheelActivity::class.java, Bundle().apply {
            putLong(SpinWheelActivity.EXTRA_WHEEL_ID, wheel.id)
        })
    }

    private fun prepareWheel(): Boolean {
        val trimmed = binding.edtName.text.toString().trim()
        if (trimmed.isBlank()) {
            Toast.makeText(this, R.string.enter_name_of_wheel, Toast.LENGTH_SHORT).show()
            return false
        }
        if (wheel.slices.size < MIN_OPTIONS) {
            Toast.makeText(this, R.string.add_at_least_two_slices, Toast.LENGTH_SHORT).show()
            return false
        }

        wheel.name = trimmed
        wheel.slices.forEach { it.label = it.label.trim() }
        return true
    }

    private fun persistWheel() {
        WheelRepository.saveWheel(this, wheel)
        originalJson = wheel.toString()
        markChanged()
    }

    private fun handleBack() {
        if (!hasChanges) {
            onBack()
            return
        }

        ConfirmDialog(
            context = this,
            title = getString(R.string.discard_changes),
            confirmText = getString(R.string.discard),
            onConfirm = { onBack() },
        ).show()
    }

    override fun onBack() {
        finishThisActivity()
    }

    private fun showColorPicker(index: Int) {
        ColorPickerDialog(
            context = this,
            selectedColor = wheel.slices[index].color,
            onConfirm = { color ->
                wheel.slices[index].color = color
                markChanged()
                renderSlices()
            },
        ).show()
    }

    private fun renderSlices() {
        binding.slicesContainer.removeAllViews()
        binding.slicesContainer.addView(addSliceRow())

        wheel.slices.forEachIndexed { index, slice ->
            binding.slicesContainer.addView(sliceRow(index, slice))
        }
    }

    private fun addSliceRow(): View {
        val row = ItemSliceBinding.inflate(layoutInflater, binding.slicesContainer, false)
        tintButton(
            row.btnEdit,
            ContextCompat.getColor(this, R.color.color_red_crimson_50),
            ContextCompat.getColor(this, R.color.white),
        )
        row.btnEdit.setImageResource(R.drawable.ic_add)
        row.btnDelete.visibility = View.INVISIBLE
        row.edtName.setText("")
        row.edtName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addOption(row.edtName.text.toString())
                true
            } else {
                false
            }
        }
        row.btnEdit.tap { addOption(row.edtName.text.toString()) }
        return row.root
    }

    private fun sliceRow(index: Int, slice: WheelSlice): View {
        val row = ItemSliceBinding.inflate(layoutInflater, binding.slicesContainer, false)
        tintButton(
            row.btnEdit,
            slice.color,
            ContextCompat.getColor(this, R.color.black),
        )
        row.btnEdit.setImageResource(R.drawable.ic_edit_white)
        row.btnEdit.tap { showColorPicker(index) }

        row.btnDelete.visibility = View.VISIBLE
        row.btnDelete.tap { deleteSlice(index) }

        row.edtName.setText(slice.label)
        row.edtName.addTextChangedListener(simpleWatcher {
            wheel.slices.getOrNull(index)?.label = row.edtName.text.toString()
            markChanged()
        })
        return row.root
    }

    private fun deleteSlice(index: Int) {
        if (wheel.slices.size <= MIN_OPTIONS) {
            Toast.makeText(this, R.string.keep_at_least_two_slices, Toast.LENGTH_SHORT).show()
            return
        }

        wheel.slices.removeAt(index)
        markChanged()
        renderSlices()
    }

    private fun tintButton(button: ImageButton, backgroundColor: Int, iconColor: Int) {
        button.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        button.imageTintList = ColorStateList.valueOf(iconColor)
    }

    private fun markChanged() {
        hasChanges = wheel.toString() != originalJson
    }

    private fun simpleWatcher(onChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChanged()
            override fun afterTextChanged(s: Editable?) = Unit
        }
    }

    companion object {
        const val EXTRA_WHEEL_ID = "extra_wheel_id"
        const val EXTRA_THEME_INDEX = "extra_theme_index"

        private const val MIN_OPTIONS = 2
        private const val MAX_OPTIONS = 24
    }
}
