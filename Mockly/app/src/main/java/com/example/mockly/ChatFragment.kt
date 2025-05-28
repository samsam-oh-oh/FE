package com.example.mockly

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mockly.adapter.ChatAdapter
import com.example.mockly.databinding.FragmentChatBinding
import com.example.mockly.model.ChatMessage
import com.example.mockly.util.HuggingFaceSTT
import com.example.mockly.util.WavRecorder
import java.io.File

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

    private var wavRecorder: WavRecorder? = null
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        checkAudioPermission()

        chatAdapter = ChatAdapter(messageList) {
            toggleRecording()
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        addInitialInterviewMessages()

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
        messageList.add(ChatMessage(
            "AI 전공 모의 면접입니다.\n약 10~15분간 진행되며 질문에 대한 정확한 답변을 말씀해주세요.\n지금부터 시작합니다...",
            isUser = false
        ))
        showCurrentQuestion()
    }

    private fun toggleRecording() {
        if (!isRecording) {
            val filename = "response_${System.currentTimeMillis()}.wav"
            wavRecorder = WavRecorder(filename, requireContext())
            wavRecorder?.startRecording()
            isRecording = true
            Toast.makeText(requireContext(), "🎤 녹음 시작", Toast.LENGTH_SHORT).show()
        } else {
            wavRecorder?.stopRecording()
            val wavFile = wavRecorder?.getWavFile()
            isRecording = false

            messageList.removeAll { it.isRecordingPrompt }

            if (wavFile != null && wavFile.exists()) {
                HuggingFaceSTT.sendAudioToHF(wavFile) { resultText ->
                    activity?.runOnUiThread {
                        messageList.add(ChatMessage(resultText, isUser = true))
                        currentQuestionIndex++
                        showCurrentQuestion()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "WAV 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                currentQuestionIndex++
                showCurrentQuestion()
            }
        }
    }

    private fun showCurrentQuestion() {
        if (currentQuestionIndex < questions.size) {
            messageList.add(ChatMessage(questions[currentQuestionIndex], isUser = false))
            messageList.add(ChatMessage("", isUser = false, isRecordingPrompt = true))
        } else {
            messageList.add(ChatMessage(
                "고생하셨습니다. 약 5분 뒤에 결과 리포트가 작성되오니 결과를 확인하세요. 모의면접 보느라 고생 많으셨습니다",
                isUser = false
            ))
        }
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "🎤 마이크 권한 허용됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "❌ 마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
