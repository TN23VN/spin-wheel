package com.example.spinwheel.ui.homograft

import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.spinwheel.R
import com.example.spinwheel.base.BaseActivity
import com.example.spinwheel.base.tap
import com.example.spinwheel.databinding.ActivityHomograftResultBinding
import com.example.spinwheel.ui.main.MainActivity
import java.io.File

class HomograftResultActivity :
    BaseActivity<ActivityHomograftResultBinding>(ActivityHomograftResultBinding::inflate) {

    private var resultFile: File? = null

    override fun getData() {
        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        resultFile = imagePath?.let(::File)?.takeIf { it.exists() }
    }

    override fun initView() {
        binding.viewTop.tvToolBar.text = getString(R.string.result)
        binding.viewTop.ivRight.setImageResource(R.drawable.ic_home_black)

        val file = resultFile
        if (file == null) {
            Toast.makeText(this, R.string.please_wait_result, Toast.LENGTH_SHORT).show()
            onBack()
            return
        }
        Glide.with(this).load(file).into(binding.ivResult)
    }

    override fun bindView() {
        binding.viewTop.ivLeft.tap { onBack() }
        binding.viewTop.ivRight.tap { goHome() }
        binding.btnTryAgain.tap { onBack() }
        binding.btnShare.tap { shareResult() }
    }

    private fun shareResult() {
        val file = resultFile ?: return
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun goHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
        finishAffinity()
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
    }
}
