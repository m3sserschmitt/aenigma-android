package com.example.enigma.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.databinding.ChatsItemBinding
import com.example.enigma.ui.ChatActivity
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import com.example.enigma.util.ContactsDiffUtil

class ChatsAdapter constructor(private val context: Context)
    : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var contacts = emptyList<ContactEntity>()

    class ChatsViewHolder(private val binding: ChatsItemBinding,
                          private val context: Context)
        :RecyclerView.ViewHolder(binding.root)
    {
        fun bind(contact: ContactEntity){

            binding.root.setOnClickListener{
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(SELECTED_CHAT_ID, binding.contact!!.address)
                context.startActivity(intent)
            }

            binding.contact = contact
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, context: Context): ChatsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChatsItemBinding.inflate(layoutInflater, parent, false)

                return ChatsViewHolder(binding, context)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder.from(parent, context)
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setData(newData: List<ContactEntity>){
        val contactsDiffUtil = ContactsDiffUtil(contacts, newData)
        val diffUtilResult = DiffUtil.calculateDiff(contactsDiffUtil)
        contacts = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
}
