package com.example.mockly.model

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isRecordingPrompt: Boolean = false // 녹음 버튼 출력용
)

