package com.example.mockly

data class RankingItem(
    val rank: Int,
    val nickname: String,
    val maxScore: Double,
    val feedback: String, // ✅ 추가
    val profileImage: String? = null
)