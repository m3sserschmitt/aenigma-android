package ro.aenigma.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.enums.NewPostSheetSection

data class NewPostSheetStateDto @OptIn(ExperimentalMaterial3Api::class) constructor(
    val sheetState: SheetValue,
    val selectedSection: NewPostSheetSection,
    val title: String,
    val fileUris: List<String>,
    val description: String,
    val content: String,
)
