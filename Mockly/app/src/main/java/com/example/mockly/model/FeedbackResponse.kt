package com.example.mockly.model

data class FeedbackResponse(
    val success: Boolean,
    val data: FeedbackData
)

data class FeedbackData(
    val feedbackList: List<String>
)

