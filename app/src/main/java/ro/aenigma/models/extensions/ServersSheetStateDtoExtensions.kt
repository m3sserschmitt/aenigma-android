package ro.aenigma.models.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.ServersSheetStateDto
import ro.aenigma.models.enums.ServersSheetSection
import ro.aenigma.util.SheetValueExtensions.isFullyExpanded
import ro.aenigma.util.SheetValueExtensions.isNotFullyExpanded

object ServersSheetStateDtoExtensions {
    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.isNotFullyExpanded(): Boolean {
        return sheetState.isNotFullyExpanded()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.isFullyExpanded(): Boolean {
        return sheetState.isFullyExpanded()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.toPartiallyExpanded(): ServersSheetStateDto {
        return copy(sheetState = SheetValue.PartiallyExpanded)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.toExpanded(): ServersSheetStateDto {
        return copy(sheetState = SheetValue.Expanded)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.toServersSection(): ServersSheetStateDto {
        return copy(selectedSection = ServersSheetSection.SERVERS)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.toServersHistorySection(): ServersSheetStateDto {
        return copy(selectedSection = ServersSheetSection.HISTORY)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.isServersHistorySection(): Boolean {
        return selectedSection == ServersSheetSection.HISTORY
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun ServersSheetStateDto.isServersSection(): Boolean {
        return selectedSection == ServersSheetSection.SERVERS
    }
}
