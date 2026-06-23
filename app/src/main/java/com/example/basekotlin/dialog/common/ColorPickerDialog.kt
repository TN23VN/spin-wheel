package com.example.spinwheel.dialog.common

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.gone
import com.example.spinwheel.base.tap
import com.example.spinwheel.base.visible
import com.example.spinwheel.databinding.DialogColorPickerBinding
import com.example.spinwheel.widget.ColorSwatchView

class ColorPickerDialog(
    context: Context,
    private val selectedColor: Int,
    private val palette: List<Int> = defaultPalette,
    private val onConfirm: (Int) -> Unit,
) : BaseDialog<DialogColorPickerBinding>(context, true) {

    private val swatches = mutableListOf<ColorSwatchView>()
    private val hsv = FloatArray(3).apply { Color.colorToHSV(selectedColor, this) }
    private var currentColor = selectedColor
    private var currentAlpha = Color.alpha(selectedColor)

    override fun setBinding(): DialogColorPickerBinding {
        return DialogColorPickerBinding.inflate(layoutInflater)
    }

    override fun initView() {
        renderPalette()
        showPalette()
        updateSelection()
    }

    override fun bindView() {
        binding.btnCancel.tap { dismiss() }
        binding.btnConfirm.tap {
            dismiss()
            onConfirm.invoke(currentColor)
        }
        binding.colorField.onColorChanged = { saturation, value ->
            hsv[1] = saturation
            hsv[2] = value
            currentColor = composeColor()
            syncCustomControls()
        }
        binding.hueSlider.onHueChanged = { hue ->
            hsv[0] = hue
            currentColor = composeColor()
            syncCustomControls()
        }
        binding.alphaSlider.onAlphaChanged = { alpha ->
            currentAlpha = alpha
            currentColor = composeColor()
            syncCustomControls()
        }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun renderPalette() {
        binding.rowTop.removeAllViews()
        binding.rowBottom.removeAllViews()
        swatches.clear()

        val colors = listOf(null) + palette.take(PRESET_COUNT)
        colors.forEachIndexed { index, color ->
            val swatch = ColorSwatchView(context).apply {
                swatchColor = color ?: currentColor
                isRainbow = color == null
                if (color == null) {
                    tap { showCustomPicker() }
                } else {
                    tap {
                        setCurrentColor(color)
                        updateSelection()
                    }
                }
            }
            swatches.add(swatch)
            addSwatch(if (index < ROW_SIZE) binding.rowTop else binding.rowBottom, swatch)
        }
    }

    private fun addSwatch(row: LinearLayout, swatch: ColorSwatchView) {
        val cell = FrameLayout(context)
        cell.addView(
            swatch,
            FrameLayout.LayoutParams(dp(SWATCH_SIZE_DP), dp(SWATCH_SIZE_DP), Gravity.CENTER),
        )
        row.addView(cell, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
    }

    private fun showPalette() {
        binding.tvTitle.setText(R.string.color)
        binding.tvMessage.visible()
        binding.colorRows.visible()
        binding.customControls.gone()
    }

    private fun showCustomPicker() {
        binding.tvTitle.setText(R.string.custom_color)
        binding.tvMessage.gone()
        binding.colorRows.gone()
        binding.customControls.visible()
        syncCustomControls()
    }

    private fun setCurrentColor(color: Int) {
        currentColor = color
        currentAlpha = Color.alpha(color)
        Color.colorToHSV(color, hsv)
    }

    private fun composeColor(): Int {
        return Color.HSVToColor(currentAlpha, hsv)
    }

    private fun syncCustomControls() {
        binding.colorField.setColor(hsv[0], hsv[1], hsv[2])
        binding.hueSlider.setHue(hsv[0])
        binding.alphaSlider.setColor(Color.HSVToColor(hsv), currentAlpha)
        updateSelection()
    }

    private fun updateSelection() {
        val selectedPaletteIndex = palette.indexOf(currentColor)
        swatches.forEachIndexed { index, swatch ->
            val isCustom = index == 0
            val isSelected = if (selectedPaletteIndex >= 0) {
                index == selectedPaletteIndex + 1
            } else {
                isCustom
            }
            swatch.swatchColor = if (isCustom) currentColor else palette[index - 1]
            swatch.isChecked = isSelected
        }
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val ROW_SIZE = 5
        private const val PRESET_COUNT = 9
        private const val SWATCH_SIZE_DP = 64

        val defaultPalette = listOf(
            Color.rgb(245, 128, 0),
            Color.rgb(0, 232, 10),
            Color.rgb(20, 0, 210),
            Color.rgb(216, 0, 6),
            Color.rgb(196, 196, 196),
            Color.rgb(255, 199, 88),
            Color.rgb(99, 245, 57),
            Color.rgb(55, 205, 224),
            Color.rgb(235, 100, 222),
        )
    }
}
