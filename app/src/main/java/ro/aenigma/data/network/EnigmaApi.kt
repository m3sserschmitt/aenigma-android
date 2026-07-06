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

package ro.aenigma.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import ro.aenigma.models.CreatedSharedDataDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.SharedDataDto
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.VertexDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.TorCheckDto
import ro.aenigma.util.Constants.Companion.FILE_API_PATH
import ro.aenigma.util.Constants.Companion.INCREMENT_FILE_COUNT_API_PAT
import ro.aenigma.util.Constants.Companion.INCREMENT_SHARE_DATA_COUNT_API_PATH
import ro.aenigma.util.Constants.Companion.SERVER_INFO_API_PATH
import ro.aenigma.util.Constants.Companion.SHARE_API_PATH
import ro.aenigma.util.Constants.Companion.VERTEX_API_PATH
import ro.aenigma.util.Constants.Companion.VERTICES_API_PATH

interface EnigmaApi {
    @GET(SERVER_INFO_API_PATH)
    suspend fun getServerInfo(): Response<ServerInfoDto?>

    @GET(VERTICES_API_PATH)
    suspend fun getVertices(): Response<List<VertexDto>?>

    @GET(SHARE_API_PATH)
    suspend fun getSharedData(@Query("Tag") tag: String): Response<SharedDataDto?>

    @POST(SHARE_API_PATH)
    suspend fun createSharedData(@Body sharedDataCreate: SharedDataCreate): Response<CreatedSharedDataDto?>

    @PUT(INCREMENT_SHARE_DATA_COUNT_API_PATH)
    suspend fun incrementSharedDataAccessCount(@Query("Tag") tag: String): Response<ResponseBody>

    @GET(VERTEX_API_PATH)
    suspend fun getVertex(@Query("Address") address: String): Response<VertexDto?>

    @Streaming
    @Multipart
    @POST(FILE_API_PATH)
    suspend fun postFile(
        @Part file: MultipartBody.Part,
        @Part("maxAccessCount") maxAccessCount: RequestBody
    ): Response<CreatedSharedDataDto?>

    @PUT(INCREMENT_FILE_COUNT_API_PAT)
    suspend fun incrementFileAccessCount(@Query("Tag") tag: String): Response<ResponseBody>

    @Streaming
    @GET(FILE_API_PATH)
    suspend fun getFile(
        @Query("tag") tag: String
    ): Response<ResponseBody>

    @GET
    suspend fun getArticlesIndex(@Url url: String): Response<List<ArticleDto>?>

    @GET
    suspend fun getText(@Url url: String): Response<String?>

    @GET
    suspend fun checkTor(@Url url: String): Response<TorCheckDto?>
}
