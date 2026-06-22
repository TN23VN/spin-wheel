package com.example.spinwheel.ui.setting

import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.gone
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivitySettingsBinding
import com.example.spinwheel.ui.about.AboutActivity
import com.example.spinwheel.util.SharedPreUtils
import com.example.spinwheel.util.rateApp
import com.example.spinwheel.util.shareApp

class SettingActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {


    override fun getData() {
        if (SharedPreUtils.getInstance().isRated(this)) {
            binding.btnRateUs.gone()
        }
    }

    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.settings)
        binding.viewTop.ivRight.inVisible()
    }

    override fun bindView() {

        binding.viewTop.ivLeft.tap { onBack() }

        binding.btnShare.tap { shareApp() }

        binding.btnRateUs.tap { rateApp(binding.btnRateUs) }

        binding.btnAbout.tap { startNextActivity(AboutActivity::class.java, null) }
    }


}
