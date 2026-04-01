package com.example.phantomlens.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.phantomlens.R

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playScreech(onCompletion: () -> Unit = {}) {
        try {
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer.create(context, R.raw.screech)
            if (mediaPlayer == null) {
                Log.e("AudioPlayer", "Failed to create MediaPlayer for R.raw.screech")
                onCompletion() // Call it anyway so the UI doesn't get stuck
                return
            }

            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                onCompletion()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing sound", e)
            onCompletion()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
