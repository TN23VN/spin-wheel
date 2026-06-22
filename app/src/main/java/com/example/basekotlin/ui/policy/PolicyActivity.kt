package com.example.spinwheel.ui.policy

import android.annotation.SuppressLint
import android.view.View
import com.example.spinwheel.R
import com.example.spinwheel.ads.IsNetWork
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.gone
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.base.visible
import com.example.spinwheel.databinding.ActivityPolicyBinding
import com.example.spinwheel.util.SettingManager

class PolicyActivity : BaseActivity<ActivityPolicyBinding>(ActivityPolicyBinding::inflate) {

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.privacy_policy)
        binding.viewTop.ivRight.inVisible()

        if (SettingManager.linkPolicy != "" && IsNetWork.haveNetworkConnection(this)) {
            binding.webView.visible()
            binding.lnNoInternet.gone()

            binding.webView.settings.javaScriptEnabled = true
            binding.webView.loadUrl(SettingManager.linkPolicy)
        } else {
            binding.webView.gone()
            binding.lnNoInternet.visible()
        }
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }
    }

}