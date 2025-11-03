package ro.aenigma.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_TABLE
import ro.aenigma.util.Constants.Companion.CONTACTS_TABLE
import ro.aenigma.util.Constants.Companion.MESSAGES_TABLE

// v1.1.0
object Migration1: Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE $MESSAGES_TABLE ADD COLUMN files TEXT")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $ATTACHMENTS_TABLE (
                messageId INTEGER NOT NULL,
                path TEXT,
                url TEXT,
                passphrase TEXT,
                PRIMARY KEY(messageId),
                FOREIGN KEY(messageId) REFERENCES $MESSAGES_TABLE(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_${CONTACTS_TABLE}_lastMessageId` ON $CONTACTS_TABLE (`lastMessageId`)")
        db.execSQL("DROP INDEX IF EXISTS `index_${CONTACTS_TABLE}_hasNewMessage_name`")
    }
}
