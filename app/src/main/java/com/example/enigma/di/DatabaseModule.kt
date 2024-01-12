package com.example.enigma.di

import android.content.Context
import androidx.room.Room
import com.example.enigma.data.database.AppDatabase
import com.example.enigma.util.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideContactsDao(database: AppDatabase) = database.contactsDao()

    @Singleton
    @Provides
    fun provideMessagesDao(database: AppDatabase) = database.messagesDao()

    @Singleton
    @Provides
    fun provideGuardsDao(database: AppDatabase) = database.guardsDao()

    @Singleton
    @Provides
    fun provideVertexDao(database: AppDatabase) = database.verticesDao()

    @Singleton
    @Provides
    fun provideEdgesDao(database: AppDatabase) = database.edgesDao()
}
