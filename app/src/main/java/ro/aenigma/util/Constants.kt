package ro.aenigma.util

class Constants {
    companion object {

        // datastore
        const val DATASTORE_PREFERENCES = "datastore-preference"
        const val ALLOW_NOTIFICATIONS_PREFERENCE = "notifications-permission"
        const val NAME_PREFERENCE = "name"

        const val ADDRESS_SIZE = 32 // bytes
        const val KEY_SIZE = 2048 // bits !!

        // Room Database
        const val DATABASE_NAME = "enigma-database"
        const val CONTACTS_TABLE = "Contacts"
        const val MESSAGES_TABLE = "Messages"
        const val CONVERSATION_PAGE_SIZE = 50
        const val GUARDS_TABLE = "Guards"
        const val VERTICES_TABLE = "Vertices"
        const val EDGES_TABLE = "Edges"
        const val GRAPH_VERSIONS_TABLE = "GraphVersions"
        const val GROUPS_TABLE = "Groups"

        const val APP_DOMAIN = "aenigma.ro"

        const val SERVER_URL = "https://$APP_DOMAIN"
    }
}
