package com.example.enigma.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.databinding.ChatsItemBinding
import com.example.enigma.util.ContactsDiffUtil

class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var contacts = emptyList<ContactEntity>()

    class ChatsViewHolder(private val binding: ChatsItemBinding)
        :RecyclerView.ViewHolder(binding.root)
    {
        fun bind(contact: ContactEntity){
            binding.contact = contact
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ChatsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChatsItemBinding.inflate(layoutInflater, parent, false)
                return ChatsViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val currentRecipe = contacts[position]
        holder.bind(currentRecipe)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setData(newData: List<ContactEntity>){
        val recipesDiffUtil =
            ContactsDiffUtil(contacts, newData)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
        contacts = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
}
