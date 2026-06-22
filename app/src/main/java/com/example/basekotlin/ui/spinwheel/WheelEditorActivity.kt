package com.example.spinwheel.ui.spinwheel

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
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
import com.example.spinwheel.dialog.common.ConfirmDialog
import com.example.spinwheel.dialog.common.SingleChoiceDialog
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
        binding.seekFontSize.progress = (wheel.fontSize - 10).coerceIn(0, 14)
        binding.seekRepeat.progress = (wheel.repeat - 1).coerceIn(0, 4)
        updateThemeLabel()
        renderSlices()
        updatePreview()
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { handleBack() }
        binding.btnSave.tap { save() }
        binding.btnPreview.tap { updatePreview() }
        binding.btnAddOption.tap { addOption() }
        binding.btnTheme.tap { showThemePicker() }

        binding.edtName.addTextChangedListener(simpleWatcher {
            wheel.name = binding.edtName.text.toString()
            markChanged()
        })
        binding.seekFontSize.setOnSeekBarChangeListener(simpleSeek { progress ->
            wheel.fontSize = progress + 10
            markChanged()
            updatePreview()
        })
        binding.seekRepeat.setOnSeekBarChangeListener(simpleSeek { progress ->
            wheel.repeat = progress + 1
            markChanged()
            updatePreview()
        })
    }

    private fun addOption() {
        if (wheel.slices.size >= 24) {
            Toast.makeText(this, R.string.max_24_options, Toast.LENGTH_SHORT).show()
            return
        }
        wheel.slices.add(WheelSlice("", WheelRepository.colorFor(wheel.themeIndex, wheel.slices.size)))
        markChanged()
        renderSlices()
        updatePreview()
    }

    private fun save() {
        val trimmed = binding.edtName.text.toString().trim()
        if (trimmed.isBlank()) {
            Toast.makeText(this, R.string.enter_name_of_wheel, Toast.LENGTH_SHORT).show()
            return
        }
        wheel.name = trimmed
        wheel.slices.forEach { it.label = it.label.trim() }
        WheelRepository.saveWheel(this, wheel)
        startNextActivity(SpinWheelActivity::class.java, Bundle().apply {
            putLong(SpinWheelActivity.EXTRA_WHEEL_ID, wheel.id)
        })
        finish()
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

    private fun showThemePicker() {
        SingleChoiceDialog(
            context = this,
            title = getString(R.string.choose_wheel_color),
            options = WheelRepository.themes,
            selectedIndex = wheel.themeIndex,
            confirmText = getString(R.string.ok),
            onConfirm = { selected ->
                wheel = WheelRepository.applyTheme(wheel, selected)
                markChanged()
                updateThemeLabel()
                renderSlices()
                updatePreview()
            },
        ).show()
    }

    private fun showColorPicker(index: Int) {
        val colors = WheelRepository.defaultColors
        val labels = colors.map { "#%06X".format(0xFFFFFF and it) }
        val selected = colors.indexOf(wheel.slices[index].color).takeIf { it >= 0 } ?: 0
        SingleChoiceDialog(
            context = this,
            title = getString(R.string.edit),
            options = labels,
            selectedIndex = selected,
            confirmText = getString(R.string.ok),
            onConfirm = { selectedColor ->
                wheel.slices[index].color = colors[selectedColor]
                markChanged()
                renderSlices()
                updatePreview()
            },
        ).show()
    }

    private fun renderSlices() {
        binding.slicesContainer.removeAllViews()
        if (wheel.slices.isEmpty()) {
            binding.slicesContainer.addView(emptyMessage())
            return
        }

        wheel.slices.forEachIndexed { index, slice ->
            binding.slicesContainer.addView(sliceRow(index, slice))
        }
    }

    private fun sliceRow(index: Int, slice: WheelSlice): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }

        val color = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
                setColor(slice.color)
            }
            setOnClickListener { showColorPicker(index) }
        }
        row.addView(color, LinearLayout.LayoutParams(dp(40), dp(40)))

        val input = EditText(this).apply {
            setText(slice.label)
            hint = getString(R.string.insert_player_name)
            setSingleLine(true)
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@WheelEditorActivity, R.color.color_main_text))
            setPadding(dp(12), 0, dp(12), 0)
            background = ContextCompat.getDrawable(this@WheelEditorActivity, R.drawable.bg_outline_red_16)
            addTextChangedListener(simpleWatcher {
                wheel.slices.getOrNull(index)?.label = text.toString()
                markChanged()
                updatePreview()
            })
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
            marginStart = dp(8)
        })

        val delete = ImageButton(this).apply {
            background = ContextCompat.getDrawable(this@WheelEditorActivity, R.drawable.bg_soft_panel_16)
            setImageResource(R.drawable.ic_delete_red)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener {
                wheel.slices.removeAt(index)
                markChanged()
                renderSlices()
                updatePreview()
                if (wheel.slices.size < 2) {
                    Toast.makeText(this@WheelEditorActivity, R.string.add_at_least_two_slices, Toast.LENGTH_SHORT).show()
                }
            }
        }
        row.addView(delete, LinearLayout.LayoutParams(dp(44), dp(44)).apply {
            marginStart = dp(8)
        })

        return row
    }

    private fun emptyMessage(): TextView {
        return TextView(this).apply {
            text = getString(R.string.add_at_least_two_slices)
            setTextColor(Color.parseColor("#990F0705"))
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(16))
        }
    }

    private fun updatePreview() {
        val repeated = mutableListOf<WheelSlice>()
        repeat(wheel.repeat.coerceAtLeast(1)) {
            repeated.addAll(wheel.slices.map { it.copy() })
        }
        binding.preview.setSlices(repeated, wheel.fontSize)
    }

    private fun updateThemeLabel() {
        binding.btnTheme.text = WheelRepository.themes.getOrElse(wheel.themeIndex) { WheelRepository.themes.first() }
    }

    private fun markChanged() {
        hasChanges = wheel.toString() != originalJson
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun simpleWatcher(onChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChanged()
            override fun afterTextChanged(s: Editable?) = Unit
        }
    }

    private fun simpleSeek(onChanged: (Int) -> Unit): android.widget.SeekBar.OnSeekBarChangeListener {
        return object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        }
    }

    companion object {
        const val EXTRA_WHEEL_ID = "extra_wheel_id"
        const val EXTRA_THEME_INDEX = "extra_theme_index"
    }
}
