package com.example.spinwheel.dialog.exit

import android.content.Context
import com.example.spinwheel.base.BaseDialog
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.DialogExitAppBinding

class ExitAppDialog(context: Context, cancelAble: Boolean?, var onClick: () -> Unit) :
    BaseDialog<DialogExitAppBinding>(context, cancelAble == true) {

    override fun setBinding(): DialogExitAppBinding {
        return DialogExitAppBinding.inflate(layoutInflater)
    }

    override fun initView() {}

    override fun bindView() {
        binding.btnCancelQuitApp.tap { dismiss() }

        binding.btnQuitApp.tap {
            dismiss()
            onClick.invoke()
        }
    }

}