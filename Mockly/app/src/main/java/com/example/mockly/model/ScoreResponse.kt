package com.example.mockly.model

data class ScoreResponse(
    val success: Boolean,
    val data: ScoreData
)

data class ScoreData(
    val scoreMap: Map<String, Int>
)

