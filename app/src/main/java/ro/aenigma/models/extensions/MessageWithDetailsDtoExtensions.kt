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

package ro.aenigma.models.extensions

import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.extensions.MessageDtoExtensions.getDateTime
import ro.aenigma.util.Constants.Companion.NEWS_FEED_TIME_PERIOD
import java.time.ZonedDateTime

object MessageWithDetailsDtoExtensions {
    @JvmStatic
    fun MessageWithDetailsDto.toArticleDto(
        uri: String?,
        files: List<String>?,
        metadata: AttachmentsMetadataDto?
    ): ArticleDto {
        return ArticleDto(
            messageId = message.id,
            title = metadata?.title,
            author = sender?.name,
            description = metadata?.description,
            url = uri,
            imageUrls = files,
            date = getDateTime().toString()
        )
    }

    @JvmStatic
    fun MessageWithDetailsDto.getDateTime(): ZonedDateTime? {
        return message.getDateTime()
    }

    @JvmStatic
    fun MessageWithDetailsDto.isWithinNewsfeedPeriod(): Boolean {
        return getDateTime()?.isAfter(ZonedDateTime.now().minus(NEWS_FEED_TIME_PERIOD)) == true
    }
}
