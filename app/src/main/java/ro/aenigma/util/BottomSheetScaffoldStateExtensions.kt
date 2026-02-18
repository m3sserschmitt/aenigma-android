package ro.aenigma.util

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import ro.aenigma.util.SheetValueExtensions.isNotFullyExpanded

object BottomSheetScaffoldStateExtensions {
    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun BottomSheetScaffoldState.isNotFullyExpanded(): Boolean {
        return bottomSheetState.currentValue.isNotFullyExpanded()
    }
}
