package com.example.spinwheel.ui.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.content.ContextCompat
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.databinding.ActivitySplashBinding
import com.example.spinwheel.ui.intro.IntroActivity
import com.example.spinwheel.ui.main.MainActivity
import com.example.spinwheel.ui.permission.PermissionActivity
import com.example.spinwheel.util.SharedPreUtils

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {


    override fun initView() {
        SharedPreUtils.getInstance().setCountOpenApp(this)

        Handler(mainLooper).postDelayed({
            startNextActivity()
        }, 1500)
    }

    private fun startNextActivity() {
        val target = when {
            !SharedPreUtils.getInstance().isFirstApp(this) -> IntroActivity::class.java
            !hasRequiredPermissions() -> PermissionActivity::class.java
            else -> MainActivity::class.java
        }
        startNextActivity(target, null)
        finishAffinity()
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBack() {}
}
