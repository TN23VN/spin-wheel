package com.example.spinwheel.dialog.choosenumber

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.DialogDareBinding

class DareDialog(
    context: Context,
    private val options: List<CharSequence>,
    private val selectionLimit: Int,
    private val onRestart: () -> Unit,
) : BaseDialog<DialogDareBinding>(context, true) {

    private val selected = BooleanArray(options.size)
    private var selectedCount = 0
    private var changingProgrammatically = false

    override fun setBinding(): DialogDareBinding {
        return DialogDareBinding.inflate(layoutInflater)
    }

    override fun initView() {
        renderOptions()
    }

    override fun bindView() {
        binding.btnRestart.tap {
            dismiss()
            onRestart.invoke()
        }
        binding.btnOk.tap { dismiss() }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun renderOptions() {
        val tint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_main))
        binding.optionsContainer.removeAllViews()
        options.forEachIndexed { index, option ->
            val checkBox = CheckBox(context).apply {
                text = option
                buttonTintList = tint
                setTextColor(ContextCompat.getColor(context, R.color.color_main_text))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                setPadding(0, dp(8), 0, dp(8))
                setOnCheckedChangeListener { button, checked ->
                    if (changingProgrammatically) return@setOnCheckedChangeListener
                    if (checked && selectedCount >= selectionLimit) {
                        changingProgrammatically = true
                        button.isChecked = false
                        changingProgrammatically = false
                        Toast.makeText(context, R.string.over_win_limit, Toast.LENGTH_SHORT).show()
                        return@setOnCheckedChangeListener
                    }
                    selectedCount += when {
                        checked && !selected[index] -> 1
                        !checked && selected[index] -> -1
                        else -> 0
                    }
                    selected[index] = checked
                }
            }
            binding.optionsContainer.addView(
                checkBox,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
