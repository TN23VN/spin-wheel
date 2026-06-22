package com.example.spinwheel.dialog.common

import android.content.Context
import android.view.ViewGroup
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.gone
import com.example.spinwheel.base.tap
import com.example.spinwheel.base.visible
import com.example.spinwheel.databinding.DialogConfirmBinding

class ConfirmDialog(
    context: Context,
    private val title: CharSequence,
    private val message: CharSequence? = null,
    private val confirmText: CharSequence = context.getString(R.string.ok),
    private val cancelText: CharSequence = context.getString(R.string.cancel),
    private val onConfirm: () -> Unit,
) : BaseDialog<DialogConfirmBinding>(context, true) {

    override fun setBinding(): DialogConfirmBinding {
        return DialogConfirmBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.tvTitle.text = title
        binding.btnCancel.text = cancelText
        binding.btnConfirm.text = confirmText
        if (message.isNullOrBlank()) {
            binding.tvMessage.gone()
        } else {
            binding.tvMessage.visible()
            binding.tvMessage.text = message
        }
    }

    override fun bindView() {
        binding.btnCancel.tap { dismiss() }
        binding.btnConfirm.tap {
            dismiss()
            onConfirm.invoke()
        }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
