package com.example.spinwheel.dialog.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.DialogSingleChoiceBinding

class SingleChoiceDialog(
    context: Context,
    private val title: CharSequence,
    private val options: List<CharSequence>,
    private val selectedIndex: Int = 0,
    private val confirmText: CharSequence = context.getString(R.string.ok),
    private val onConfirm: (Int) -> Unit,
) : BaseDialog<DialogSingleChoiceBinding>(context, true) {

    private var currentIndex = 0

    override fun setBinding(): DialogSingleChoiceBinding {
        return DialogSingleChoiceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        currentIndex = selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))
        binding.tvTitle.text = title
        binding.btnConfirm.text = confirmText
        renderOptions()
    }

    override fun bindView() {
        binding.btnCancel.tap { dismiss() }
        binding.btnConfirm.tap {
            dismiss()
            onConfirm.invoke(currentIndex)
        }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun renderOptions() {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_main))
        binding.radioGroup.removeAllViews()
        options.forEachIndexed { index, option ->
            val radioButton = RadioButton(context).apply {
                id = View.generateViewId()
                text = option
                tag = index
                buttonTintList = tint
                setTextColor(ContextCompat.getColor(context, R.color.color_main_text))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                setPadding(0, dp(10), 0, dp(10))
                isChecked = index == currentIndex
            }
            binding.radioGroup.addView(
                radioButton,
                RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            currentIndex = group.findViewById<RadioButton>(checkedId)?.tag as? Int ?: currentIndex
        }
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
