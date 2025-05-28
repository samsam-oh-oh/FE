package com.example.mockly.util

import android.util.Log
import com.example.mockly.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object HuggingFaceSTT {

    // ✅ Hugging Face Inference API 호출 함수
    fun sendAudioToHF(wavFile: File, onResult: (String) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val mediaType = "audio/wav".toMediaTypeOrNull()

        // ✅ 파일 존재 확인
        if (!wavFile.exists()) {
            Log.e("HF_STT", "❌ 파일 없음: ${wavFile.absolutePath}")
            onResult("파일을 찾을 수 없습니다.")
            return
        }

        Log.d("HF_STT", "📦 전송할 파일 경로: ${wavFile.absolutePath}, 크기: ${wavFile.length()} bytes")

        val requestBody = wavFile.asRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://yi0xv2xd2ecifua7.us-east-1.aws.endpoints.huggingface.cloud") // ✅ Inference API Endpoint
            .addHeader("Authorization", "Bearer ${BuildConfig.HF_API_TOKEN}")
            .addHeader("Content-Type", "audio/wav")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HF_STT", "❌ 요청 실패", e)
                onResult("요청 실패: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("HF_STT", "📝 응답 본문: $body")

                if (response.isSuccessful && !body.isNullOrBlank()) {
                    onResult(body)
                } else {
                    onResult("인식 실패 또는 응답 없음")
                }
            }
        })
    }
}
