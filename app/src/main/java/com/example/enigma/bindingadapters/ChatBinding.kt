package com.example.enigma.bindingadapters

import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import java.text.SimpleDateFormat

class ChatBinding {

    companion object {

        @BindingAdapter("messageDateBinding")
        @JvmStatic
        fun setMessageDate(textView: TextView, message: MessageEntity?)
        {
            if(message != null) {
                textView.text = SimpleDateFormat("dd.MM.yyyy hh:mm").format(message.date)
            }
        }

        @BindingAdapter("messageContentBinding")
        @JvmStatic
        fun setMessageContent(textView: TextView, message: MessageEntity?)
        {
            if(message != null)
            {
                textView.text = message.text
            }
        }

        @BindingAdapter("contactNameBinding")
        @JvmStatic
        fun setContactName(toolbar: Toolbar, contact: ContactEntity?)
        {
            if(contact != null)
            {
                toolbar.title = contact.name
            }
        }
    }
}
