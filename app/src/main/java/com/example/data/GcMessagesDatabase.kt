package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        BurnerContactEntity::class,
        TacticalPinEntity::class,
        ScheduledSmsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class GcMessagesDatabase : RoomDatabase() {
    abstract fun dao(): GcMessagesDao

    companion object {
        @Volatile
        private var INSTANCE: GcMessagesDatabase? = null

        fun getInstance(context: Context): GcMessagesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GcMessagesDatabase::class.java,
                    "gc_messages_sqlcipher_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
