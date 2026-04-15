package ro.aenigma.models.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.enums.NewPostSheetSection
import ro.aenigma.util.SheetValueExtensions.isFullyExpanded
import ro.aenigma.util.SheetValueExtensions.isNotFullyExpanded

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
                id = null,
                title = title,
                description = description,
                imageUrls = fileUris,
                url = null,
                date = null
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
