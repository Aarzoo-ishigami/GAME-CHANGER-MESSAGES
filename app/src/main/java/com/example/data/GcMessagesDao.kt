package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GcMessagesDao {

    @Query("SELECT * FROM conversations WHERE isDecoyRecord = :isDecoy ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun observeConversations(isDecoy: Boolean): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET customNickname = :newNickname WHERE id = :conversationId")
    suspend fun updatePeerNickname(conversationId: String, newNickname: String)

    @Query("UPDATE conversations SET preferredSimSlot = :simSlot WHERE id = :conversationId")
    suspend fun updatePreferredSim(conversationId: String, simSlot: Int)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :conversationId")
    suspend fun updateConversationPinned(conversationId: String, isPinned: Boolean)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markConversationRead(conversationId: String)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isDecoyRecord = :isDecoy ORDER BY timestamp ASC")
    fun observeMessagesForConversation(conversationId: String, isDecoy: Boolean): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageForConversation(conversationId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    // Self-Destructing Message Queries
    @Query("SELECT * FROM messages WHERE expiresAtTimestamp IS NOT NULL AND expiresAtTimestamp <= :nowMillis")
    suspend fun getExpiredSelfDestructMessages(nowMillis: Long): List<MessageEntity>

    @Query("UPDATE messages SET swarmTransferredChunks = :transferredChunks, transferSpeedMbps = :speedMbps, transportMode = :transportMode WHERE id = :messageId")
    suspend fun updateSwarmTransferProgress(
        messageId: String,
        transferredChunks: Int,
        speedMbps: Int,
        transportMode: String
    )

    // Burner Contacts (Strictly isolated from device ContactsContract)
    @Query("SELECT * FROM burner_contacts ORDER BY createdAt DESC")
    fun observeBurnerContacts(): Flow<List<BurnerContactEntity>>

    @Query("SELECT * FROM burner_contacts WHERE id = :burnerId LIMIT 1")
    suspend fun getBurnerContactById(burnerId: String): BurnerContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBurnerContact(burner: BurnerContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBurnerContacts(burners: List<BurnerContactEntity>)

    @Query("DELETE FROM burner_contacts WHERE id = :burnerId")
    suspend fun deleteBurnerContactById(burnerId: String)

    @Query("SELECT * FROM burner_contacts WHERE expiresAt IS NOT NULL AND expiresAt <= :nowMillis")
    suspend fun getExpiredBurnerContacts(nowMillis: Long): List<BurnerContactEntity>

    @Query("SELECT * FROM tactical_pins ORDER BY timestamp DESC")
    fun observeTacticalPins(): Flow<List<TacticalPinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTacticalPin(pin: TacticalPinEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTacticalPins(pins: List<TacticalPinEntity>)

    @Query("DELETE FROM tactical_pins WHERE id = :pinId")
    suspend fun deleteTacticalPin(pinId: String)

    @Query("SELECT * FROM scheduled_sms ORDER BY scheduledEpochMillis ASC")
    fun observeScheduledSms(): Flow<List<ScheduledSmsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledSms(item: ScheduledSmsEntity)

    @Query("UPDATE scheduled_sms SET status = :status WHERE id = :id")
    suspend fun updateScheduledSmsStatus(id: String, status: String)

    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int
}
