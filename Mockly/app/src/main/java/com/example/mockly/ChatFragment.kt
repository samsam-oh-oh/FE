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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        chatAdapter = ChatAdapter(messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        binding.sendButton.setOnClickListener {
            val userMsg = binding.messageInput.text.toString()
            if (userMsg.isNotBlank()) {
                messageList.add(ChatMessage(userMsg, true))
                messageList.add(ChatMessage("이건 예시 답변입니다.", false)) // ChatGPT 응답 대체용
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
}
