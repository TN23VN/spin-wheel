package com.example.spinwheel.ui.main

import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.inVisible
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityMainBinding
import com.example.spinwheel.dialog.exit.ExitAppDialog
import com.example.spinwheel.ui.choosenumber.ChooseNumberActivity
import com.example.spinwheel.ui.homograft.HomograftActivity
import com.example.spinwheel.ui.setting.SettingActivity
import com.example.spinwheel.ui.spinwheel.SpinWheelActivity


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initView() {
        binding.viewTop.ivLeft.inVisible()
        binding.viewTop.tvToolBar.text = getString(R.string.app_name_2)
        binding.viewTop.ivRight.setImageResource(R.drawable.ic_settings_red)
    }

    override fun bindView() {
        binding.viewTop.ivRight.tap { startNextActivity(SettingActivity::class.java, null) }
        binding.btnSpinRoulette.tap { startNextActivity(SpinWheelActivity::class.java, null) }
        binding.btnLetsSpin.tap { startNextActivity(SpinWheelActivity::class.java, null) }
        binding.btnHomograft.tap { startNextActivity(HomograftActivity::class.java, null) }
        binding.btnChooseNumber.tap { startNextActivity(ChooseNumberActivity::class.java, null) }
    }

    private fun showDialogQuit() {
        val dialogQuit = ExitAppDialog(this, false, onClick = {
            finishAffinity()
        })
        dialogQuit.show()
    }

    override fun onBack() {
        showDialogQuit()
    }

}
