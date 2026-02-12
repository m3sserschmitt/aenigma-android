package ro.aenigma.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE

// v1.2.0
object Migration3: Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM `$GUARDS_TABLE`;")
        db.execSQL("DROP INDEX IF EXISTS `index_${GUARDS_TABLE}_address`;")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_${GUARDS_TABLE}_address` ON `$GUARDS_TABLE` (`address`);")
    }
}
