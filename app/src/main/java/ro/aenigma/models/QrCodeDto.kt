package ro.aenigma.models

import android.graphics.Bitmap

data class QrCodeDto(
    val code: Bitmap,
    val label: String,
    val isOwnCode: Boolean
)
