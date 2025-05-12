package com.example.mockly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mockly.databinding.FragmentChatBinding
import com.example.mockly.model.ChatMessage
import com.example.mockly.adapter.ChatAdapter

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private val questions = listOf(
        "자기소개를 부탁드립니다.",
        "지원 동기를 말씀해주세요.",
        "가장 힘들었던 경험은 무엇인가요?",
        "마지막 질문입니다. 우리 회사에 바라는 점이 있나요?"
    )
    private var currentQuestionIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        chatAdapter = ChatAdapter(messageList) {
            toggleRecording()
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        // 초기 면접 안내 + 첫 질문 + 녹음 버튼
        addInitialInterviewMessages()

        // 일반 메시지 전송
        binding.sendButton.setOnClickListener {
            val userMsg = binding.messageInput.text.toString()
            if (userMsg.isNotBlank()) {
                messageList.add(ChatMessage(userMsg, true))
                messageList.add(ChatMessage("이건 예시 답변입니다.", false))
                chatAdapter.notifyDataSetChanged()
                binding.messageInput.text.clear()
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addInitialInterviewMessages() {
        messageList.add(ChatMessage("AI 전공 모의 면접입니다.\n약 10~15분간 진행되며 질문에 대한 정확한 답변을 말씀해주세요.\n지금부터 시작합니다...", isUser = false))
        messageList.add(ChatMessage(questions[currentQuestionIndex], isUser = false))
        messageList.add(ChatMessage("", isUser = false, isRecordingPrompt = true))
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun toggleRecording() {
        // 녹음 종료 후 다음 질문으로 진행
        messageList.add(ChatMessage("음성파일.avi", isUser = true))

        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            messageList.add(ChatMessage(questions[currentQuestionIndex], isUser = false))
            messageList.add(ChatMessage("", isUser = false, isRecordingPrompt = true))
        } else {
            messageList.add(ChatMessage("고생하셨습니다. 약 5분 뒤에 결과 리포트가 작성되오니 결과를 확인하세요. 모의면접 보느라 고생 많으셨습니다", isUser = false))
        }

        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
    }
}
