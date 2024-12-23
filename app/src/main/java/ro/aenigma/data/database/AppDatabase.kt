package ro.aenigma.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        GuardEntity::class,
        VertexEntity::class,
        EdgeEntity::class,
        GraphVersionEntity::class],
    version = 1,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    abstract fun messagesDao(): MessagesDao

    abstract fun guardsDao(): GuardsDao

    abstract fun verticesDao(): VerticesDao

    abstract fun edgesDao(): EdgesDao

    abstract fun graphVersionsDao(): GraphVersionsDao
}
