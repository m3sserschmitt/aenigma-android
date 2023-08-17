package com.example.enigma.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

class ChatRowBinding {

    companion object
    {
        @BindingAdapter("setMessageDate")
        @JvmStatic
        fun setMessageDate(textView: TextView, date: Date)
        {
            textView.text = SimpleDateFormat("dd.MM.yyyy hh:mm").format(date)
        }
    }
}
