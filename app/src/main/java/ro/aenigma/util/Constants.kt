package ro.aenigma.util

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
        const val NEWS_FEED_SIZE = 150

        const val GUARDS_TABLE = "Guards"
        const val VERTICES_TABLE = "Vertices"
        const val EDGES_TABLE = "Edges"
        const val GROUPS_TABLE = "Groups"
        const val ATTACHMENTS_TABLE = "Attachments"

        const val APP_DOMAIN = "aenigma.ro"

        const val API_BASE_URL = "https://$APP_DOMAIN/"

        const val ARTICLES_BASE_URL = "https://articles.$APP_DOMAIN/"

        const val ARTICLES_INDEX_FILE_TEMPLATE = "index-%s.json"

        const val SHARED_FILES_FEED_WEIGHT = 0.65

        const val ARTICLES_FEED_WEIGHT = 0.35

        const val SOCKS5_PROXY_ADDRESS = "127.0.0.1"
        const val SOCKS5_PROXY_PORT = 9050

        const val ATTACHMENTS_METADATA_FILE = "metadata.json"
        const val ENCRYPTION_KEY_SIZE = 32

        const val ATTACHMENT_DOWNLOAD_NOTIFICATION_ID = 105

        const val GRAPH_READER_NOTIFICATION_ID = 100

        const val GROUP_DOWNLOAD_NOTIFICATION_ID = 101

        const val GROUP_UPLOAD_NOTIFICATION_ID = 102

        const val MESSAGE_SENDER_NOTIFICATION_ID = 103

        const val SIGNALR_NOTIFICATION_ID = 104

        const val ATTACHMENT_BIN_PACKING_SIZE = 15L * 1024 * 1024
    }
}
