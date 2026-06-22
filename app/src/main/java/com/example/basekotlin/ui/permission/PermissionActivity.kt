package com.example.spinwheel.ui.permission

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityPermissionBinding
import com.example.spinwheel.ui.main.MainActivity
import com.example.spinwheel.base.BaseActivity

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    private lateinit var prefs: SharedPreferences
    private lateinit var cameraLauncher: ActivityResultLauncher<String>
    private lateinit var locationLauncher: ActivityResultLauncher<String>
    private lateinit var notificationLauncher: ActivityResultLauncher<String>

    override fun initView() {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        cameraLauncher = registerPermissionLauncher(PermissionItem.CAMERA)
        locationLauncher = registerPermissionLauncher(PermissionItem.LOCATION)
        notificationLauncher = registerPermissionLauncher(PermissionItem.NOTIFICATION)
        updateSwitchStates()
    }

    override fun bindView() {
        binding.tvContinue.tap {
            startNextActivity()
        }
        binding.switchNoti.setOnClickListener {
            handlePermissionClick(PermissionItem.NOTIFICATION)
        }
        binding.switchMemory.setOnClickListener {
            handlePermissionClick(PermissionItem.LOCATION)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            updateSwitchStates()
        }
    }

    private fun registerPermissionLauncher(permissionItem: PermissionItem): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                increaseDenyCount(permissionItem)
            }
            updateSwitchStates()
        }
    }

    private fun handlePermissionClick(permissionItem: PermissionItem) {
        if (isGranted(permissionItem)) {
            updateSwitchStates()
            return
        }

        if (getDenyCount(permissionItem) >= MAX_DENY_COUNT) {
            updateSwitchStates()
            openAppSettings()
            return
        }

        permissionItem.launcher().launch(permissionItem.permission)
    }

    private fun updateSwitchStates() {
        updateSwitch(binding.switchCamera, PermissionItem.CAMERA)
        updateSwitch(binding.switchLocation, PermissionItem.LOCATION)
        updateSwitch(binding.switchNoti, PermissionItem.NOTIFICATION)
    }

    private fun updateSwitch(switch: SwitchCompat, permissionItem: PermissionItem) {
        val granted = isGranted(permissionItem)
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = granted
        switch.isEnabled = !granted
        switch.setOnClickListener {
            handlePermissionClick(permissionItem)
        }
    }

    private fun isGranted(permissionItem: PermissionItem): Boolean {
        if (permissionItem == PermissionItem.NOTIFICATION && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            this,
            permissionItem.permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun PermissionItem.launcher(): ActivityResultLauncher<String> {
        return when (this) {
            PermissionItem.CAMERA -> cameraLauncher
            PermissionItem.LOCATION -> locationLauncher
            PermissionItem.NOTIFICATION -> notificationLauncher
        }
    }

    private fun getDenyCount(permissionItem: PermissionItem): Int {
        return prefs.getInt(permissionItem.denyKey, 0)
    }

    private fun increaseDenyCount(permissionItem: PermissionItem) {
        prefs.edit()
            .putInt(permissionItem.denyKey, getDenyCount(permissionItem) + 1)
            .apply()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun startNextActivity() {
        startNextActivity(MainActivity::class.java, null)
        finishAffinity()
    }

    override fun onBack() {
        finishAffinity()
    }

    private enum class PermissionItem(
        val permission: String,
        val denyKey: String,
    ) {
        CAMERA(Manifest.permission.CAMERA, "camera_deny_count"),
        LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, "location_deny_count"),
        NOTIFICATION(Manifest.permission.POST_NOTIFICATIONS, "notification_deny_count"),
    }

    private companion object {
        const val PREFS_NAME = "permission_denied_count"
        const val MAX_DENY_COUNT = 2
    }
}
