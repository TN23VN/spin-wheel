package com.example.spinwheel.ui.splash

import android.os.Handler
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.databinding.ActivitySplashBinding
import com.example.spinwheel.ui.language.LanguageStartActivity
import com.example.spinwheel.util.SharedPreUtils

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {


    override fun initView() {
        SharedPreUtils.getInstance().setCountOpenApp(this)

        Handler(mainLooper).postDelayed({
            startNextActivity()
        }, 1500)
    }

    private fun startNextActivity() {
        startNextActivity(LanguageStartActivity::class.java, null)
        finishAffinity()
    }

    override fun onBack() {}
}