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
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.data.database.migrations.Migration1
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
        var dbPassphrase: ByteArray? = null
        var lockedDbPassphrase: ByteArray? = null
        try {
            System.loadLibrary("sqlcipher")
            lockedDbPassphrase = DbPassphraseKeeper.dbPassphrase.value ?: throw Exception("Database passphrase not ready.")
            dbPassphrase = CryptoProvider.masterKeyDecrypt(lockedDbPassphrase) ?: throw Exception("Failed to unlock database passphrase.")
            val supportFactory = SupportOpenHelperFactory(dbPassphrase.clone())
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(Migration1)
                .openHelperFactory(supportFactory)
                .build()
        } finally {
            dbPassphrase?.fill(0)
            lockedDbPassphrase?.fill(0)
            DbPassphraseKeeper.dbPassphrase.value = null
        }
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
}
