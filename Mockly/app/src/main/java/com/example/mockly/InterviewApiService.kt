package com.example.mockly.api

import com.example.mockly.model.EvaluationRequest
import com.example.mockly.model.EvaluationResponse
import com.example.mockly.model.QuestionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface InterviewApiService {

    // ✅ 1. 질문 생성 API - PDF 파일 업로드
    @Multipart
    @POST("llm/upload/pdf")
    fun generateQuestions(
        @Part file: MultipartBody.Part
    ): Call<Void>  // ← 여기만 수정

    // ✅ 2. 질문 목록 조회 API
    @GET("llm/questions")
    fun getQuestions(): Call<QuestionResponse>



    // ✅ 3. 답변 평가 API
    @POST("interview/evaluate")
    fun evaluateAnswers(
        @Body request: EvaluationRequest
    ): Call<EvaluationResponse>
}
