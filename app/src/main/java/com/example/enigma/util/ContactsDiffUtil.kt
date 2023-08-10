package com.example.enigma.util

import androidx.recyclerview.widget.DiffUtil
import com.example.enigma.data.database.ContactEntity

class ContactsDiffUtil(
    private val oldList: List<ContactEntity>,
    private val newList: List<ContactEntity>
): DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}