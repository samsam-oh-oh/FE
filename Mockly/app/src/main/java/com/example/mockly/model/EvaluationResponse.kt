package com.example.mockly.model

data class EvaluationResponse(
    val feedback: String,
    val scores: Map<String, Int>
)
