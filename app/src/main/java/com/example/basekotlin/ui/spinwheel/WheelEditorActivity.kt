package com.example.spinwheel.ui.spinwheel

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.example.spinwheel.ui.main.MainActivity

class WheelEditorActivity :
    BaseActivity<ActivityWheelEditorBinding>(ActivityWheelEditorBinding::inflate) {

    private lateinit var wheel: WheelModel
    private var originalJson = ""
    private var hasChanges = false
    private var returnHomeOnBack = false
    private var isPreviewMode = false

    override fun getData() {
        val id = intent.getLongExtra(EXTRA_WHEEL_ID, -1L)
        returnHomeOnBack = intent.getBooleanExtra(EXTRA_RETURN_HOME_ON_BACK, false)
        wheel = if (id != -1L) {
            WheelRepository.getWheel(this, id) ?: WheelRepository.getDefaultWheel(this)
        } else {
            WheelRepository.newWheel(intent.getIntExtra(EXTRA_THEME_INDEX, 0))
        }
        originalJson = wheelSnapshot()
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
        updatePreview()
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { handleBack() }
        binding.btnSave.tap { save() }
        binding.btnPreview.tap { previewWheel() }
        binding.btnPreviewSave.tap { save() }
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
                color = randomOptionColor(),
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
        if (!canShowPreview()) return

        updatePreview()
        showPreviewMode()
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
        originalJson = wheelSnapshot()
        markChanged()
    }

    private fun handleBack() {
        if (isPreviewMode) {
            showEditMode()
            return
        }

        if (!hasChanges) {
            leaveEditorWithoutSaving()
            return
        }

        confirmLeave()
    }

    override fun onBack() {
        handleBack()
    }

    private fun showColorPicker(index: Int) {
        ColorPickerDialog(
            context = this,
            selectedColor = wheel.slices[index].color,
            title = getString(R.string.edit),
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

        if (wheel.slices.isEmpty()) {
            binding.slicesContainer.addView(emptySlicesView())
        } else {
            wheel.slices.forEachIndexed { index, slice ->
                binding.slicesContainer.addView(sliceRow(index, slice))
            }
        }
        updatePreview()
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
        row.btnEdit.isEnabled = row.edtName.text.toString().isNotBlank()
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
            updatePreview()
        })
        return row.root
    }

    private fun deleteSlice(index: Int) {
        wheel.slices.removeAt(index)
        markChanged()
        renderSlices()
    }

    private fun emptySlicesView(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(18))

            addView(
                ImageView(context).apply {
                    setImageResource(R.drawable.spin_wheel)
                    alpha = 0.72f
                },
                LinearLayout.LayoutParams(dp(76), dp(76)),
            )

            addView(
                TextView(context).apply {
                    text = getString(R.string.add_at_least_two_slices)
                    gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(context, R.color.color_text_second))
                    textSize = 14f
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    topMargin = dp(10)
                },
            )
        }
    }

    private fun updatePreview() {
        binding.wheelPreview.setSlices(wheel.slices, wheel.fontSize)
    }

    private fun canShowPreview(): Boolean {
        if (binding.edtName.text.toString().trim().isBlank()) {
            Toast.makeText(this, R.string.enter_name_of_wheel, Toast.LENGTH_SHORT).show()
            return false
        }
        if (wheel.slices.size < MIN_OPTIONS) {
            Toast.makeText(this, R.string.add_at_least_two_slices, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showPreviewMode() {
        isPreviewMode = true
        binding.viewTop.tvToolBar.text = getString(R.string.edit_wheel)
        binding.editScroll.visibility = View.GONE
        binding.previewContainer.visibility = View.VISIBLE
    }

    private fun showEditMode() {
        isPreviewMode = false
        binding.viewTop.tvToolBar.text = if (intent.hasExtra(EXTRA_WHEEL_ID)) {
            getString(R.string.edit_wheel)
        } else {
            getString(R.string.new_wheel)
        }
        binding.previewContainer.visibility = View.GONE
        binding.editScroll.visibility = View.VISIBLE
    }

    private fun randomOptionColor(): Int {
        return WheelRepository.defaultColors.random()
    }

    private fun leaveEditorWithoutSaving() {
        if (returnHomeOnBack) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
            )
            finish()
        } else {
            finishThisActivity()
        }
    }

    private fun tintButton(button: ImageButton, backgroundColor: Int, iconColor: Int) {
        button.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        button.imageTintList = ColorStateList.valueOf(iconColor)
    }

    private fun markChanged() {
        hasChanges = wheelSnapshot() != originalJson
    }

    private fun wheelSnapshot(): String = wheel.toString()

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun simpleWatcher(onChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChanged()
            override fun afterTextChanged(s: Editable?) = Unit
        }
    }

    private fun confirmLeave() {
        ConfirmDialog(
            img =R.drawable.ic_warring,
            context = this,
            title = getString(R.string.warning),
            message = getString(R.string.if_you_leave_now),
            confirmText = getString(R.string.exit),
            onConfirm = {
                leaveEditorWithoutSaving()
            },
        ).show()
    }

    companion object {
        const val EXTRA_WHEEL_ID = "extra_wheel_id"
        const val EXTRA_THEME_INDEX = "extra_theme_index"
        const val EXTRA_RETURN_HOME_ON_BACK = "extra_return_home_on_back"

        private const val MIN_OPTIONS = 2
        private const val MAX_OPTIONS = 24
    }
}
