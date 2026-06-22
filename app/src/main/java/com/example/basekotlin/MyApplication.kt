package com.example.spinwheel

import android.app.Application
import com.example.spinwheel.util.SharedPreUtils
import com.facebook.FacebookSdk


class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        SharedPreUtils.init(this)

        FacebookSdk.sdkInitialize(applicationContext)

    }


}