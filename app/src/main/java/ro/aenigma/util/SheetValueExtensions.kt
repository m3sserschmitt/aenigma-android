package ro.aenigma.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue

object SheetValueExtensions {
    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun SheetValue.isNotFullyExpanded(): Boolean {
        return this == SheetValue.Hidden || this == SheetValue.PartiallyExpanded
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @JvmStatic
    fun SheetValue.isFullyExpanded(): Boolean {
        return this == SheetValue.Expanded
    }
}
