package com.example.spinwheel.base

import android.app.Dialog
import android.content.Context
import android.view.Window
import androidx.viewbinding.ViewBinding
import com.example.spinwheel.R
import com.example.spinwheel.util.SystemUtil

abstract class BaseDialog<VB : ViewBinding>(context: Context, canAble: Boolean) :
    Dialog(context, R.style.BaseDialog) {

    var binding: VB
    protected abstract fun setBinding(): VB

    init {
        SystemUtil.setLocale(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = setBinding()
        setContentView(binding.root)
        setCancelable(canAble)

        initView()
        bindView()
    }

    open fun initView() {

    }

    open fun bindView() {

    }

}