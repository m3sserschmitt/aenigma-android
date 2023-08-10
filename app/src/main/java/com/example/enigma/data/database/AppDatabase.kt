package com.example.enigma.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

//    companion object {
//        private const val databaseName = "enigma-database"
//
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            if (INSTANCE == null) {
//                synchronized(this) {
//                    INSTANCE =
//                        Room.databaseBuilder(context,AppDatabase::class.java, databaseName)
//                            .build()
//                }
//            }
//
//            return INSTANCE!!
//        }
//    }

    abstract fun contactsDao(): ContactsDao
}
