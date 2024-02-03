package com.example.enigma.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.databinding.ContactItemBinding
import com.example.enigma.ui.fragments.contacts.ContactsFragmentDirections
import com.example.enigma.util.ContactsDiffUtil

class ContactsAdapter constructor(private val navController: NavController)
    : RecyclerView.Adapter<ContactsAdapter.ChatsViewHolder>() {

    private var contacts = emptyList<ContactEntity>()

    class ChatsViewHolder(private val binding: ContactItemBinding,
                          private val navController: NavController)
        :RecyclerView.ViewHolder(binding.root)
    {
        fun bind(contact: ContactEntity){

            binding.root.setOnClickListener{
                val navigation = ContactsFragmentDirections.actionContactsFragmentToChatFragment(contact.address)
                navController.navigate(navigation)
            }

            binding.contact = contact
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, navController: NavController): ChatsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContactItemBinding.inflate(layoutInflater, parent, false)

                return ChatsViewHolder(binding, navController)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder.from(parent, navController)
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
