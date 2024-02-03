package com.example.enigma.bindingadapters

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

class AddContactsBinding {

    companion object {

        @BindingAdapter("qrCodeBinding")
        @JvmStatic
        fun setQrCode(imageView: ImageView, bitmap: Bitmap?)
        {
            if(bitmap != null)
            {
                imageView.setImageBitmap(bitmap)
            }
        }
    }
}
