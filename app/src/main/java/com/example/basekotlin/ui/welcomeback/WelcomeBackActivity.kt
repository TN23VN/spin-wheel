package com.example.spinwheel.ui.welcomeback

import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityWelcomeBackBinding

class WelcomeBackActivity :
    BaseActivity<ActivityWelcomeBackBinding>(ActivityWelcomeBackBinding::inflate) {

    override fun bindView() {
        binding.tvContinue.tap {
            finishThisActivity()
        }
    }

    override fun onBack() {
        finishAffinity()
    }
}
