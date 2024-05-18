package com.example.enigma.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.enigma.ui.MainActivity

fun Context.findActivity(): MainActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context as MainActivity
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.permissionGranted(permission: String): Boolean
{
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.notificationsPermissionGranted(): Boolean
{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.permissionGranted(Manifest.permission.POST_NOTIFICATIONS)
    else
        true
}

fun Context.openApplicationDetails()
{
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", this.packageName, null)
    )
    this.startActivity(intent)
}
