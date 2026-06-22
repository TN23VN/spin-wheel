package com.example.spinwheel.ui.setting

import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.gone
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivitySettingsBinding
import com.example.spinwheel.ui.about.AboutActivity
import com.example.spinwheel.ui.language.LanguageActivity
import com.example.spinwheel.util.InsertListManager
import com.example.spinwheel.util.SharedPreUtils
import com.example.spinwheel.util.SystemUtil
import com.example.spinwheel.util.feedbackApp
import com.example.spinwheel.util.rateApp
import com.example.spinwheel.util.shareApp

class SettingActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {


    override fun getData() {
        val codeLang = SystemUtil.getPreLanguage(this)
        binding.tvLang.text = InsertListManager.getListLanguage(this@SettingActivity).find { it.code == codeLang }?.name ?: ""

        if (SharedPreUtils.getInstance().isRated(this)) {
            binding.btnRateUs.gone()
        }
    }

    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.settings)
        binding.viewTop.tvToolBar.isAllCaps = false
        binding.viewTop.ivLeft.gone()
        binding.viewTop.ivRight.gone()

    }

    override fun bindView() {

        binding.btnLanguage.tap { startNextActivity(LanguageActivity::class.java, null) }

        binding.btnShare.tap { shareApp() }

        binding.btnRateUs.tap { rateApp(binding.btnRateUs) }

        binding.btnFeedback.tap { feedbackApp() }

        binding.btnAbout.tap { startNextActivity(AboutActivity::class.java, null) }
    }


}