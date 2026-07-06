/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
