package com.example.spinwheel.ui.main

import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.databinding.ActivityMainBinding
import com.example.spinwheel.dialog.exit.ExitAppDialog


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {


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
