package com.example.spinwheel.ui.language

import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityLanguageBinding
import com.example.spinwheel.ui.language.adapter.LanguageAdapter
import com.example.spinwheel.ui.main.MainActivity
import com.example.spinwheel.util.InsertListManager
import com.example.spinwheel.util.SystemUtil

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {

    private var codeLang: String? = null

    override fun initView() {
        codeLang = SystemUtil.getPreLanguage(this)
        binding.viewTop.tvToolBar.text = getString(R.string.language)

        binding.rcvLang.apply {
            adapter = LanguageAdapter { codeLang = it }.apply {
                addListData(InsertListManager.getListLanguage(this@LanguageActivity))
                setCheck(codeLang)
            }

        }
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }

        binding.viewTop.ivRight.tap {
            SystemUtil.saveLocale(this, codeLang)

            onNextActivity()
        }
    }

    private fun onNextActivity() {
        startNextActivity(MainActivity::class.java, null)
        finishAffinity()
    }

}
