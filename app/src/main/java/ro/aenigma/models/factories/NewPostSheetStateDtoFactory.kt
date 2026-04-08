package ro.aenigma.models.factories

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.enums.NewPostSheetSection
import kotlin.collections.listOf

object NewPostSheetStateDtoFactory {
    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun create(sheetState: SheetValue, selectedSection: NewPostSheetSection): NewPostSheetStateDto {
        return NewPostSheetStateDto(
            sheetState = sheetState,
            selectedSection = selectedSection,
            title = "",
            fileUris = listOf(),
            description = "",
            content = "",
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun create(): NewPostSheetStateDto {
        return create(
            sheetState = SheetValue.PartiallyExpanded,
            selectedSection = NewPostSheetSection.EDIT
        )
    }
}
