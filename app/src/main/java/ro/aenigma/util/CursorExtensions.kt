package ro.aenigma.util

import android.database.Cursor

fun Cursor.getFirstLong(): Long = if (moveToFirst()) getLong(0) else -1L
