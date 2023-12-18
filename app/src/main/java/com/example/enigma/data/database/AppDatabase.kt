package com.example.enigma.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        KeyPairEntity::class,
        GuardEntity::class,
        VertexEntity::class,
        EdgeEntity::class],
    version = 2,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    abstract fun messagesDao(): MessagesDao

    abstract fun keysDao(): KeyPairsDao

    abstract fun guardsDao(): GuardsDao

    abstract fun verticesDao(): VerticesDao

    abstract fun edgesDao(): EdgesDao
}
