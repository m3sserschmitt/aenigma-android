package com.example.enigma.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.example.enigma.ui.MainActivity

fun Context.findActivity(): MainActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context as MainActivity
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}
