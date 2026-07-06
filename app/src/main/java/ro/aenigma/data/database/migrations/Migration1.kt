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
