package ro.aenigma.di

import android.content.Context
import androidx.room.Room
import ro.aenigma.data.database.AppDatabase
import ro.aenigma.util.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import ro.aenigma.crypto.CryptoProvider
import javax.inject.Singleton

object DbPassphraseKeeper {
    var dbPassphrase = MutableStateFlow<ByteArray?>(null)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val key = CryptoProvider.masterKeyDecrypt(
            DbPassphraseKeeper.dbPassphrase.value
                ?: throw Exception("Database passphrase not ready.")
        )
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(SupportFactory(key)).build()
    }

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

    @Singleton
    @Provides
    fun provideGraphVersionsDao(database: AppDatabase) = database.graphVersionsDao()
}
