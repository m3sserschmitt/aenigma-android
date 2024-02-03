package com.example.enigma.bindingadapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.enigma.data.database.ContactEntity

class ContactsBinding {

    companion object
    {
        @BindingAdapter("messageNotReadBinding")
        @JvmStatic
        fun setNewMessageReceived(imageView: ImageView, contact: ContactEntity?)
        {
            if(contact == null)
            {
                return
            }

            if(contact.hasNewMessage)
            {
                imageView.visibility = ImageView.VISIBLE
            } else {
                imageView.visibility = ImageView.INVISIBLE
            }
        }

        @BindingAdapter("contactNameAdapter")
        @JvmStatic
        fun setMessageSender(textView: TextView, contact: ContactEntity?)
        {
            if(contact != null)
            {
                textView.text = contact.name
            }
        }
    }
}
