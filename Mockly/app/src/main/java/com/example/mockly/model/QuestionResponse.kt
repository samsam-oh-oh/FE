package com.example.mockly.model

import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: QuestionData?
)

data class QuestionData(
    @SerializedName("status")
    val status: String?,
    @SerializedName("questionList")
    val questionList: List<String>?
)
