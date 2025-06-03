package com.example.mockly.util

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object CustomSTT {

    fun sendAudioToCustomSTT(wavFile: File, onResult: (String) -> Unit) {
        // ✅ Logging Interceptor 추가
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        if (!wavFile.exists()) {
            Log.e("CustomSTT", "❌ 파일 없음: ${wavFile.absolutePath}")
            onResult("파일을 찾을 수 없습니다.")
            return
        }

        Log.d("CustomSTT", "📦 전송할 파일 경로: ${wavFile.absolutePath}, 크기: ${wavFile.length()} bytes")

        // ⚠️ 백엔드에서 요구하는 필드명이 "file"이 맞는지 확인 필요!
        val requestFile = wavFile.asRequestBody("audio/wav".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", wavFile.name, requestFile)
            .build()

        val request = Request.Builder()
            .url("http://54.180.37.66:8001/transcribe-audio") // ✅ POST 엔드포인트
            .post(multipartBody)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CustomSTT", "❌ 요청 실패", e)
                onResult("요청 실패: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                Log.d("CustomSTT", "📝 응답 본문: $bodyString")

                if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                    try {
                        val json = JSONObject(bodyString)
                        val transcript = json.optString("cleaned_transcription", "응답에서 텍스트를 찾을 수 없습니다.")
                        onResult(transcript)
                    } catch (e: Exception) {
                        Log.e("CustomSTT", "❌ JSON 파싱 실패", e)
                        onResult("응답 파싱 오류: ${e.localizedMessage}")
                    }
                } else {
                    Log.e("CustomSTT", "❌ 응답 실패 코드: ${response.code}")
                    onResult("인식 실패 또는 응답 없음")
                }
            }
        })
    }

    fun fetchTranscriptionFromServer(onResult: (String) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://54.180.37.66:8001/get-transcription")
            .get()
            .addHeader("Accept", "text/plain")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CustomSTT", "❌ GET 요청 실패", e)
                onResult("요청 실패: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("CustomSTT", "📥 GET 응답 본문: $body")

                if (response.isSuccessful && !body.isNullOrBlank()) {
                    onResult(body)
                } else {
                    Log.e("CustomSTT", "❌ GET 응답 실패 코드: ${response.code}")
                    onResult("인식 실패 또는 응답 없음")
                }
            }
        })
    }
}
