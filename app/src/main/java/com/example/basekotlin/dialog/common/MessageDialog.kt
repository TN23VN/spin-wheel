package com.example.spinwheel.dialog.common

import android.content.Context
import android.view.ViewGroup
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.DialogMessageBinding

class MessageDialog(
    context: Context,
    private val title: CharSequence,
    private val message: CharSequence,
    private val onOk: () -> Unit = {},
) : BaseDialog<DialogMessageBinding>(context, true) {

    override fun setBinding(): DialogMessageBinding {
        return DialogMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.tvTitle.text = title
        binding.tvMessage.text = message
    }

    override fun bindView() {
        binding.btnOk.tap {
            dismiss()
            onOk.invoke()
        }
    }

    override fun show() {
        super.show()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
