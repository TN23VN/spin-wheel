package com.example.spinwheel.util

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class SoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var mediaPlayer: MediaPlayer? = null

    fun play(@RawRes resId: Int, loop: Boolean = false) {
        stop()
        mediaPlayer = MediaPlayer.create(appContext, resId)?.apply {
            isLooping = loop
            setOnCompletionListener {
                if (!isLooping) {
                    this@SoundPlayer.stop()
                }
            }
            start()
        }
    }

    fun stop() {
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() {
        stop()
    }
}
