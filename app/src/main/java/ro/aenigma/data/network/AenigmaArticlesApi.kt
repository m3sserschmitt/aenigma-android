/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2023-2026 Romulus-Emanuel Ruja <romulus.ruja@aenigma.ro>

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

package ro.aenigma.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url
import ro.aenigma.models.ArticleDto
import ro.aenigma.util.Constants.Companion.ADD_CONTACTS_HELP_API_PATH
import ro.aenigma.util.Constants.Companion.ARTICLES_INDEX_API_PATH
import ro.aenigma.util.Constants.Companion.CHAT_HELP_API_PATH
import ro.aenigma.util.Constants.Companion.CONTACTS_HELP_API_PATH
import ro.aenigma.util.Constants.Companion.FEED_HELP_API_PATH
import ro.aenigma.util.Constants.Companion.NEW_POST_SHEET_HELP_API_PATH
import ro.aenigma.util.Constants.Companion.PRIVACY_POLICY_API_PATH
import ro.aenigma.util.Constants.Companion.SERVERS_SHEET_HELP_API_PATH

interface AenigmaArticlesApi {

    @GET(ARTICLES_INDEX_API_PATH)
    suspend fun getArticlesIndex(@Path("lang") languageCode: String): Response<List<ArticleDto>?>

    @GET(CONTACTS_HELP_API_PATH)
    suspend fun getContactsScreenHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(SERVERS_SHEET_HELP_API_PATH)
    suspend fun getServersSheetHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(CHAT_HELP_API_PATH)
    suspend fun getChatScreenHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(ADD_CONTACTS_HELP_API_PATH)
    suspend fun getAddContactsHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(FEED_HELP_API_PATH)
    suspend fun getFeedScreenHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(NEW_POST_SHEET_HELP_API_PATH)
    suspend fun getNewPostSheetHelp(@Path("lang") languageCode: String): Response<String?>

    @GET(PRIVACY_POLICY_API_PATH)
    suspend fun getPrivacyPolicy(@Path("lang") languageCode: String): Response<String?>

    @GET
    suspend fun getText(@Url url: String): Response<String?>
}
