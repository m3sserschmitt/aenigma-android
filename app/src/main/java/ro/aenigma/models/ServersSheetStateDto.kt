package ro.aenigma.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.enums.ServersSheetSection

data class ServersSheetStateDto @OptIn(ExperimentalMaterial3Api::class) constructor(
    val sheetState: SheetValue,
    val selectedSection: ServersSheetSection
)
