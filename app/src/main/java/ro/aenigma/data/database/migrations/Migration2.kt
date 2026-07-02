/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
