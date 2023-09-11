package com.example.enigma.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.databinding.ChatItemBinding
import com.example.enigma.util.MessagesDiffUtil

class ChatAdapter
    : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages = emptyList<MessageEntity>()

    class ChatViewHolder(private val binding: ChatItemBinding)
        : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(message: MessageEntity)
        {
            if(message.incoming)
            {
                binding.contentTextView.updateLayoutParams<ConstraintLayout.LayoutParams>
                {
                    horizontalBias = 1f
                }
                binding.messageDateTextView.updateLayoutParams<ConstraintLayout.LayoutParams>
                {
                    horizontalBias = 1f
                }
            }

            binding.message = message
            binding.executePendingBindings()
        }

        fun resetAlignment()
        {
            binding.contentTextView.updateLayoutParams<ConstraintLayout.LayoutParams>
            {
                horizontalBias = 0f
            }
            binding.messageDateTextView.updateLayoutParams<ConstraintLayout.LayoutParams>
            {
                horizontalBias = 0f
            }
        }

        companion object {
            fun from(parent: ViewGroup) : ChatViewHolder
            {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChatItemBinding.inflate(layoutInflater, parent, false)

                return ChatViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun onViewRecycled(holder: ChatViewHolder)
    {
        holder.resetAlignment()
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun setData(newData: List<MessageEntity>)
    {
        val messagesDiffUtil = MessagesDiffUtil(messages, newData)
        val diffUtilResult = DiffUtil.calculateDiff(messagesDiffUtil)

        messages = newData

        diffUtilResult.dispatchUpdatesTo(this)
    }
}
