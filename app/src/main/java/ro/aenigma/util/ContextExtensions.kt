package ro.aenigma.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import ro.aenigma.ui.AppActivity
import ro.aenigma.util.ContentResolverExtensions.querySize
import androidx.core.net.toUri
import ro.aenigma.util.Constants.Companion.ATTACHMENT_BIN_PACKING_SIZE
import ro.aenigma.util.FileExtensions.lengthSafe
import java.io.File

object ContextExtensions {
    private fun Context.sizeOf(uriString: String): Long {
        val uri = uriString.toUri()

        return when {
            uri.scheme?.equals(ContentResolver.SCHEME_CONTENT, true) == true ->
                contentResolver.querySize(uri)

            uri.scheme?.equals(ContentResolver.SCHEME_FILE, true) == true ->
                File(uri.path ?: return -1).lengthSafe()

            uri.scheme == null -> File(uriString).lengthSafe()

            else -> -1L
        }
    }

    fun Context.splitFilesFirstFitDecreasing(
        uriStrings: List<String>,
        limitBytes: Long = ATTACHMENT_BIN_PACKING_SIZE
    ): List<List<String>> {

        /* ---------- 1. Build [Entry] list with sizes ---------- */
        val entries = uriStrings.map { Entry(it, sizeOf(it)) }

        if(entries.any { entry -> entry.size < 0 }) {
            return listOf()
        }

        /* ---------- 2. Sort descending by size (FFD) ---------- */
        val sorted = entries.sortedByDescending { it.size }

        /* ---------- 3. Bin‑packing, first‑fit ---------- */
        val bins = mutableListOf<Bin>()

        for (e in sorted) {
            // oversize: isolate so caller can handle/reject later
            if (e.size >= limitBytes) {
                bins += Bin(mutableListOf(e.str), e.size)
                continue
            }

            val fit = bins.firstOrNull { it.canFit(e.size, limitBytes) }
            if (fit != null) fit.add(e) else bins += Bin(mutableListOf(e.str), e.size)
        }

        /* ---------- 4. Return just the string groups ---------- */
        return bins.map { it.items }
    }

    private data class Entry(val str: String, val size: Long)

    private data class Bin(
        val items: MutableList<String>,
        var currentSize: Long = 0
    ) {
        fun canFit(extra: Long, limit: Long) = currentSize + extra < limit
        fun add(e: Entry) {
            items += e.str
            currentSize += e.size
        }
    }

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
}
