package com.example.spinwheel.dialog.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.spinwheel.R
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.BottomSheetNumberPickerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class NumberPickerBottomSheet(
    private val context: Context,
    private val title: CharSequence,
    private val options: List<Int>,
    selectedValue: Int,
    private val onSelected: (Int) -> Unit,
) {

    private val dialog = BottomSheetDialog(context)
    private val binding = BottomSheetNumberPickerBinding.inflate(LayoutInflater.from(context))
    private val rowHeight = dp(context, ROW_HEIGHT_DP)
    private var currentValue = selectedValue

    init {
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        dialog.behavior.skipCollapsed = true
        binding.tvTitle.text = title
        binding.btnClose.tap { dialog.dismiss() }
        renderOptions()

        dialog.setOnShowListener {
            dialog.window?.setDimAmount(DIM_AMOUNT)
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundColor(Color.TRANSPARENT)
            scrollToSelected()
        }
    }

    fun show() {
        dialog.show()
    }

    private fun renderOptions() {
        val selectedColor = ContextCompat.getColor(context, R.color.color_main)
        val defaultColor = ContextCompat.getColor(context, R.color.color_text_second)
        val font = ResourcesCompat.getFont(context, R.font.inter_semibold)
            ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        binding.optionsContainer.removeAllViews()
        options.forEach { option ->
            val isSelected = option == currentValue
            val optionView = TextView(context).apply {
                text = option.toString()
                gravity = Gravity.CENTER
                includeFontPadding = false
                typeface = font
                setTextColor(if (isSelected) selectedColor else defaultColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isSelected) 24f else 18f)
                background = if (isSelected) {
                    ContextCompat.getDrawable(context, R.drawable.bg_number_picker_selected)
                } else {
                    null
                }
                tap {
                    if (option != currentValue) {
                        currentValue = option
                        onSelected(option)
                        renderOptions()
                        scrollToSelected()
                    }
                }
            }

            binding.optionsContainer.addView(
                optionView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    rowHeight,
                ),
            )
        }
    }

    private fun scrollToSelected() {
        val selectedIndex = options.indexOf(currentValue).coerceAtLeast(0)
        val scrollY = (selectedIndex - 1).coerceAtLeast(0) * rowHeight
        binding.optionsScroll.post {
            binding.optionsScroll.scrollTo(0, scrollY)
        }
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    private companion object {
        private const val ROW_HEIGHT_DP = 40
        private const val DIM_AMOUNT = 0.55f
    }
}
