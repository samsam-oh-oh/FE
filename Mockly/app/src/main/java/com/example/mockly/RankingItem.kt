package com.example.mockly

data class RankingItem(
    val rank: Int,
    val nickname: String,
    val totalScore: Double,
    val techScore: Double,
    val communicateScore: Double,
    val feedback: String,
    val unlocked: Boolean,
    val userPoint: Int = 0
)