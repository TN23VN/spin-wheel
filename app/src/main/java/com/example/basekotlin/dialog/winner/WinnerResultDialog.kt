package com.example.spinwheel.dialog.winner

import android.content.Context
import android.view.ViewGroup
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.DialogWinnerResultBinding

class WinnerResultDialog(
    context: Context,
    private val winner: String,
    private val onRemove: () -> Unit,
) : BaseDialog<DialogWinnerResultBinding>(context, true) {

    override fun setBinding(): DialogWinnerResultBinding {
        return DialogWinnerResultBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.tvWinner.text = winner
    }

    override fun bindView() {
        binding.btnClose.tap { dismiss() }
        binding.btnRemove.tap {
            dismiss()
            onRemove.invoke()
        }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
