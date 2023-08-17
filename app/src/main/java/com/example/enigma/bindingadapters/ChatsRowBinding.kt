package com.example.enigma.bindingadapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter

class ChatsRowBinding {

    companion object
    {
        @BindingAdapter("setNewMessageReceived")
        @JvmStatic
        fun setNewMessageReceived(imageView: ImageView, hasNewMessage: Boolean)
        {
            if(hasNewMessage)
            {
                imageView.visibility = ImageView.VISIBLE
            } else {
                imageView.visibility = ImageView.INVISIBLE
            }
        }
    }
}