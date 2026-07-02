package ro.aenigma.models.extensions
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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.enums.NewPostSheetSection
import ro.aenigma.util.SheetValueExtensions.isFullyExpanded
import ro.aenigma.util.SheetValueExtensions.isNotFullyExpanded
import ro.aenigma.util.ZonedDateTimeExtensions.socialMediaStyleFormat
import java.time.ZonedDateTime

object NewPostSheetStateDtoExtensions {
    object ServersSheetStateDtoExtensions {
        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.isNotFullyExpanded(): Boolean {
            return sheetState.isNotFullyExpanded()
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.isFullyExpanded(): Boolean {
            return sheetState.isFullyExpanded()
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.toPartiallyExpanded(): NewPostSheetStateDto {
            return copy(sheetState = SheetValue.PartiallyExpanded)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.toExpanded(): NewPostSheetStateDto {
            return copy(sheetState = SheetValue.Expanded)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.toEditSection(): NewPostSheetStateDto {
            return copy(selectedSection = NewPostSheetSection.EDIT)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.toCoverPreviewSection(): NewPostSheetStateDto {
            return copy(selectedSection = NewPostSheetSection.COVER_PREVIEW)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.toContentPreviewSection(): NewPostSheetStateDto {
            return copy(selectedSection = NewPostSheetSection.CONTENT_PREVIEW)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.isCoverPreviewSection(): Boolean {
            return selectedSection == NewPostSheetSection.COVER_PREVIEW
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.isContentPreviewSection(): Boolean {
            return selectedSection == NewPostSheetSection.CONTENT_PREVIEW
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @JvmStatic
        fun NewPostSheetStateDto.isEditSection(): Boolean {
            return selectedSection == NewPostSheetSection.EDIT
        }

        @JvmStatic
        fun NewPostSheetStateDto.toArticleDto(): ArticleDto {
            return ArticleDto(
                messageId = null,
                title = title,
                description = description,
                imageUrls = fileUris,
                url = null,
                date = ZonedDateTime.now().socialMediaStyleFormat()
            )
        }

        @JvmStatic
        fun NewPostSheetStateDto.toAttachmentsMetadata(): AttachmentsMetadataDto {
            return AttachmentsMetadataDto(
                title = title,
                description = description
            )
        }
    }
}
