package com.example.spinwheel.ui.intro

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.spinwheel.ui.permission.PermissionActivity
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tapNoHandle
import com.example.spinwheel.model.IntroModel
import com.example.spinwheel.databinding.ActivityIntroBinding
import com.example.spinwheel.ui.intro.adapter.IntroAdapter
import com.example.spinwheel.ui.main.MainActivity
import com.example.spinwheel.util.SharedPreUtils
import kotlin.math.abs

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private var dots: Array<ImageView>? = null
    private var listIntro: ArrayList<IntroModel> = ArrayList()

    override fun initView() {
        dots = arrayOf(binding.ivCircle01, binding.ivCircle02, binding.ivCircle03)

        listIntro.add(
            IntroModel(
                R.string.title_intro_01, R.string.content_intro_01, R.drawable.bg_intro_item_1
            )
        )
        listIntro.add(
            IntroModel(
                R.string.title_intro_02, R.string.content_intro_02, R.drawable.bg_intro_item_2
            )
        )
        listIntro.add(
            IntroModel(
                R.string.title_intro_03, R.string.content_intro_03, R.drawable.bg_intro_item_3
            )
        )

        val introAdapter = IntroAdapter()
        introAdapter.addListData(listIntro)
        binding.viewPager2.adapter = introAdapter

    }

    override fun bindView() {
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(100))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.8f + r * 0.2f
            val absPosition = abs(position)
            page.alpha = 1.0f - (1.0f - 0.3f) * absPosition
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeContentInit(position)

                when (position) {
                    0, 1 -> {
                        binding.btnNext.tapNoHandle {
                            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
                        }
                    }

                    2 -> {
                        binding.btnNext.tapNoHandle {
                            startNextActivity()
                        }
                    }
                }
            }
        })
    }

    private fun startNextActivity() {
        SharedPreUtils.getInstance().setFirstApp(this)
        if (hasRequiredPermissions()) {
            startNextActivity(MainActivity::class.java, null)
        } else {
            startNextActivity(PermissionActivity::class.java, null)
        }
        finish()
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun changeContentInit(position: Int) {
        for (i in 0..2) {
            if (i == position) dots!![i].setImageResource(R.drawable.ic_intro_s) else dots!![i].setImageResource(
                R.drawable.ic_intro_sn
            )
        }
    }

    override fun onStart() {
        super.onStart()
        changeContentInit(binding.viewPager2.currentItem)
    }

    override fun onBack() {
        finishAffinity()
    }
}
