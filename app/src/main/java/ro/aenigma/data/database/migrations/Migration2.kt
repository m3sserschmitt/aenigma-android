package ro.aenigma.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE
import ro.aenigma.util.Constants.Companion.VERTICES_TABLE

// v1.2.0
object Migration2: Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `$GUARDS_TABLE`;")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `${GUARDS_TABLE}` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `address` TEXT NOT NULL,
                `publicKey` TEXT NOT NULL,
                `hostname` TEXT,
                `onionService` TEXT,
                `graphVersion` TEXT,
                `dateCreated` TEXT NOT NULL
            );
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_${GUARDS_TABLE}_address` ON `${GUARDS_TABLE}` (`address`);")
        db.execSQL("ALTER TABLE `$VERTICES_TABLE` ADD COLUMN onionService TEXT;")
    }
}
