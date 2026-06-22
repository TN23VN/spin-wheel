package com.example.spinwheel.base

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.example.spinwheel.R
import com.example.spinwheel.util.SystemUtil

abstract class BaseActivity<VB : ViewBinding>(val bindingFactory: (LayoutInflater) -> VB) :
    AppCompatActivity() {

    protected val binding: VB by lazy { bindingFactory(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        SystemUtil.setLocale(this)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.hideNavigation()

        getData()
        initView()
        bindView()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    open fun getData() {

    }

    open fun initView() {

    }

    open fun bindView() {

    }

    open fun reloadAds() {

    }


    fun startNextActivity(activity: Class<*>?, bundle: Bundle?) {
        var bundle = bundle
        val intent = Intent(this, activity)
        if (bundle == null) {
            bundle = Bundle()
        }
        intent.putExtras(bundle)
        resultLauncher.launch(intent)
        overridePendingTransition(R.anim.in_right, R.anim.out_left)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadAds()
            }
        }

    fun finishThisActivity() {
        finish()
        overridePendingTransition(R.anim.in_left, R.anim.out_right)
    }

    open fun onBack() {
        setResult(RESULT_OK)
        finishThisActivity()
    }
}
