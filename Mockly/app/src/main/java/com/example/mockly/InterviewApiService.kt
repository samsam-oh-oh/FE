package com.example.mockly.api


import com.example.mockly.model.FeedbackResponse
import com.example.mockly.model.QuestionResponse
import com.example.mockly.model.ScoreResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

    @Multipart
    @POST("llm/upload/qa")
    fun uploadAnswerTextWithToken(
        @Part STT_file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<ResponseBody>



    @GET("llm/feedbacks")
    fun getFeedbacks(@QueryMap memberOpt: Map<String, String>): Call<FeedbackResponse>


    @GET("/llm/scores")
    fun getScores(): Call<ScoreResponse>  // ✅ 올바른 타입




}
