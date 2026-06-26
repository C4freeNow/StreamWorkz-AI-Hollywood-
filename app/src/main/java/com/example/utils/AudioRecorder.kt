package com.example.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording(): File? {
        val outputDir = context.cacheDir
        try {
            audioFile = File.createTempFile("journal_audio", ".3gp", outputDir)
        } catch (e: IOException) {
            Log.e("AudioRecorder", "Failed to create temp audio file", e)
            return null
        }

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to initialize and start recording", e)
            mediaRecorder = null
            audioFile = null
        }
        return audioFile
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recording", e)
        }
        mediaRecorder = null
        val recordedFile = audioFile
        audioFile = null
        return recordedFile
    }
}
