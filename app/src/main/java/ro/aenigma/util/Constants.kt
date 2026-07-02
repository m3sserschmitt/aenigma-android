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

package ro.aenigma.util

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.unit.dp
import okhttp3.Dns
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Duration

class Constants {
    companion object {

        // datastore
        const val DATASTORE_PREFERENCES = "datastore-preference"

        const val ADDRESS_SIZE_BYTES = 32
        const val KEY_SIZE_BITS = 4096

        // Room Database
        const val DATABASE_NAME = "enigma-database"
        const val CONTACTS_TABLE = "Contacts"
        const val MESSAGES_TABLE = "Messages"
        const val CONVERSATION_PAGE_SIZE = 50
        const val SERVERS_LIST_SIZE = 150
        const val NEWS_FEED_SIZE = 1000
        const val SEND_MESSAGES_CHUNK_SIZE = 15
        const val CONTACTS_LIST_SIZE = 150
        const val OK_HTTP_CONNECT_TIMEOUT: Long = 30_000
        const val SIGNALR_SERVER_TIMEOUT_INTERVAL: Long = 90_000
        const val SIGNALR_KEEP_ALIVE_INTERVAL: Long = 20_000
        const val SIGNALR_HANDSHAKE_TIMEOUT: Long = 15_000
        const val CLIENT_METHOD_INVOCATION_TIMEOUT: Long = 20_000
        const val REQUEST_BODY_BUFFER_SIZE_OVER_TOR = 262_144
        const val REQUEST_BODY_DEFAULT_BUFFER_SIZE = 1_048_576
        const val GUARDS_TABLE = "Guards"
        const val VERTICES_TABLE = "Vertices"
        const val EDGES_TABLE = "Edges"
        const val GROUPS_TABLE = "Groups"
        const val ATTACHMENTS_TABLE = "Attachments"

        const val APP_DEEP_LINK_SCHEME = "aenigma"

        const val APP_DOMAIN = "aenigma.ro"

        const val ARTICLES_DOMAIN = "articles.$APP_DOMAIN"

        const val WEB_DOMAIN = "web.$APP_DOMAIN"

        const val API_BASE_URL = "https://$APP_DOMAIN/"

        const val ARTICLES_INDEX_URL_TEMPLATE = "https://$ARTICLES_DOMAIN/index-%s.json"

        const val PRIVACY_POLICY_URL_TEMPLATE = "https://$ARTICLES_DOMAIN/privacy-policy-%s.md"

        const val WEB_ARTICLE_URL_TEMPLATE = "https://$WEB_DOMAIN/#/blog/article?url=%s"

        const val DEFAULT_LANGUAGE_CODE = "en"

        const val IMAGES_CACHE_DIRECTORY = "images_cache"

        const val NEWS_FEED_DIRECTORY = "newsfeed"

        const val ORBOT_PACKAGE = "org.torproject.android"

        const val PRIVATE_KEY_FILE = "private-key.locked"

        const val PUBLIC_KEY_FILE = "public-key.pem"

        const val LOCAL_MEDIA_FEED_WEIGHT = 3

        const val WEB_ARTICLES_FEED_WEIGHT = 1

        const val TOR_PROXY_HOSTNAME = "127.0.0.1"

        const val TOR_SOCKS5_PROXY_PORT = 9050

        val TOR_PROXY = Proxy(
            Proxy.Type.SOCKS,
            InetSocketAddress.createUnresolved(TOR_PROXY_HOSTNAME, TOR_SOCKS5_PROXY_PORT)
        )

        val TOR_DNS = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return listOf(InetAddress.getByAddress(hostname, byteArrayOf(0, 0, 0, 0)))
            }
        }

        const val CHECK_TOR_URL = "https://check.torproject.org/api/ip"

        const val ENCRYPTION_KEY_SIZE = 32

        const val ATTACHMENT_MAX_SIZE = 26_214_400L

        const val ATTACHMENTS_MAX_COUNT = 25

        const val AUTHENTICATION_DEADLINE = 60_000L

        const val IMAGE_COMPRESSION_QUALITY = 50

        const val GUARDS_HISTORY_SIZE = 150

        val NAVIGATION_BAR_HEIGHT = IntrinsicSize.Min

        val BOTTOM_SHEET_PEEK_HEIGHT = 45.dp

        val INFO_SCREEN_ICON_SIZE = 50.dp

        const val SERVER_INFO_API_PATH = "/Info"

        const val VERTICES_API_PATH = "/Vertices"

        const val SHARE_API_PATH = "/Share"

        const val INCREMENT_SHARE_DATA_COUNT_API_PATH = "/IncrementSharedDataAccessCount"

        const val VERTEX_API_PATH = "/Vertex"

        const val FILE_API_PATH = "/File"

        const val INCREMENT_FILE_COUNT_API_PAT = "/IncrementFileAccessCount"

        const val ONION_ROUTING_ENDPOINT = "OnionRouting"

        const val GENERATE_NONCE_METHOD = "GenerateToken"

        const val AUTHENTICATE_METHOD = "Authenticate"

        const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        const val PULL_METHOD = "Pull2"

        const val CLEANUP_METHOD = "Cleanup"

        const val BROADCAST_CONTACT_ADDRESS = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"

        const val MARKDOWN_FILE_EXTENSION = "md"

        const val JSON_FILE_EXTENSION = "json"

        val NEWS_FEED_TIME_PERIOD: Duration = Duration.ofDays(30)
    }
}
