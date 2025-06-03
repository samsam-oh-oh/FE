package com.example.mockly

data class RankingItem(
    val rank: Int,
    val nickname: String,
    val maxScore: Double,
    val feedback: String,
    val profileImage: String? = null,
    val userPoint: Int = 0 // ✅ 추가
)