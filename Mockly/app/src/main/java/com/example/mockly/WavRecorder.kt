package com.example.mockly.util

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavRecorder(
    private val fileName: String,
    private val appContext: android.content.Context
) {

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private lateinit var outputFile: File
    private lateinit var rawFile: File

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("WavRecorder", "🚫 RECORD_AUDIO permission not granted.")
            return
        }

        val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (dir != null && !dir.exists()) dir.mkdirs()
        outputFile = File(dir, "$fileName.wav")
        rawFile = File(dir, "$fileName.raw")

        try {
            audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioRecord?.startRecording()
            isRecording = true

            Thread {
                writeAudioDataToFile()
                writeWavHeader()
            }.start()

        } catch (e: Exception) {
            Log.e("WavRecorder", "❌ 녹음 시작 실패: ${e.localizedMessage}")
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun writeAudioDataToFile() {
        val outputStream = BufferedOutputStream(FileOutputStream(rawFile))
        val buffer = ByteArray(bufferSize)

        try {
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    outputStream.write(buffer, 0, read)
                }
            }
            outputStream.close()
        } catch (e: IOException) {
            Log.e("WavRecorder", "❗ 음성 데이터 저장 실패", e)
        }
    }

    private fun writeWavHeader() {
        try {
            val rawData = rawFile.readBytes()
            val wavOut = FileOutputStream(outputFile)

            val totalDataLen = rawData.size + 36
            val byteRate = sampleRate * 2

            val header = ByteBuffer.allocate(44)
            header.order(ByteOrder.LITTLE_ENDIAN)

            header.put("RIFF".toByteArray())
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(1)
            header.putShort(1)
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort(2)
            header.putShort(16)
            header.put("data".toByteArray())
            header.putInt(rawData.size)

            wavOut.write(header.array())
            wavOut.write(rawData)
            wavOut.close()

            rawFile.delete()
            Log.d("WavRecorder", "✅ WAV 파일 저장 완료: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("WavRecorder", "❌ WAV 헤더 작성 실패", e)
        }
    }

    fun getWavFile(): File = outputFile
}
