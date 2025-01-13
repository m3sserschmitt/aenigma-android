package ro.aenigma.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import ro.aenigma.ui.AppActivity

fun Context.findActivity(): AppActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context as AppActivity
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.openApplicationDetails()
{
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", this.packageName, null)
    )
    this.startActivity(intent)
}
