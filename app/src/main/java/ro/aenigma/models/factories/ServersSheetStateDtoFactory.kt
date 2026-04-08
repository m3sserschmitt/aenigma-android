package ro.aenigma.models.factories

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.ServersSheetStateDto
import ro.aenigma.models.enums.ServersSheetSection

object ServersSheetStateDtoFactory {
    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun create(): ServersSheetStateDto {
        return ServersSheetStateDto(
            sheetState = SheetValue.PartiallyExpanded,
            selectedSection = ServersSheetSection.SERVERS
        )
    }
}
