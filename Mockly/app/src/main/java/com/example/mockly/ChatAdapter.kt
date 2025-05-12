package com.example.mockly.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mockly.databinding.ItemChatBotBinding
import com.example.mockly.databinding.ItemChatUserBinding
import com.example.mockly.databinding.ItemChatRecordButtonBinding
import com.example.mockly.model.ChatMessage

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val onRecordClick: () -> Unit // 녹음 버튼 클릭 콜백
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
        private const val TYPE_RECORD_BUTTON = 2
    }

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        return when {
            msg.isRecordingPrompt -> TYPE_RECORD_BUTTON
            msg.isUser -> TYPE_USER
            else -> TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val binding = ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserViewHolder(binding)
            }

            TYPE_BOT -> {
                val binding = ItemChatBotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BotViewHolder(binding)
            }

            TYPE_RECORD_BUTTON -> {
                val binding = ItemChatRecordButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RecordButtonViewHolder(binding, onRecordClick)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.binding.userMessage.text = message.message
            is BotViewHolder -> holder.binding.botMessage.text = message.message
            is RecordButtonViewHolder -> { /* 버튼에 대한 바인딩은 내부에서 처리됨 */ }
        }
    }

    class UserViewHolder(val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root)
    class BotViewHolder(val binding: ItemChatBotBinding) : RecyclerView.ViewHolder(binding.root)
    class RecordButtonViewHolder(
        val binding: ItemChatRecordButtonBinding,
        val onRecordClick: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.recordButton.setOnClickListener {
                onRecordClick()
            }
        }
    }
}
