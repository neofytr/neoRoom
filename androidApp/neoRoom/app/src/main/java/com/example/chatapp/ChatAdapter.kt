package com.example.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemReceivedMessageBinding
import com.example.chatapp.databinding.ItemSentMessageBinding

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUser: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val showSender = shouldShowSender(position)

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message, showSender)
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sender == currentUser) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    private fun shouldShowSender(position: Int): Boolean {
        // Show sender name if this is the first message or if the previous message was from a different sender
        if (position == 0) return true
        val currentSender = messages[position].sender
        val previousSender = messages[position - 1].sender
        return currentSender != previousSender
    }

    inner class SentMessageViewHolder(
        private val binding: ItemSentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.messageText.text = message.content
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemReceivedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage, showSender: Boolean) {
            binding.messageText.text = message.content

            if (showSender) {
                binding.senderName.visibility = View.VISIBLE
                binding.senderName.text = message.sender
            } else {
                binding.senderName.visibility = View.GONE
            }

            // Set bubble color based on sender
            binding.messageBubble.setCardBackgroundColor(message.color)
        }
    }
}