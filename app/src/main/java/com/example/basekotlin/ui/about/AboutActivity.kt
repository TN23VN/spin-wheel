package com.example.spinwheel.ui.about

import android.annotation.SuppressLint
import android.view.View
import com.example.spinwheel.BuildConfig
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityAboutBinding
import com.example.spinwheel.ui.policy.PolicyActivity

class AboutActivity : BaseActivity<ActivityAboutBinding>(ActivityAboutBinding::inflate) {

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.about)
        binding.viewTop.ivRight.inVisible()

        binding.tvVersion.text = getString(R.string.version) + " ${BuildConfig.VERSION_NAME}"
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }

        binding.tvPolicy.tap { startNextActivity(PolicyActivity::class.java, null) }
    }

}