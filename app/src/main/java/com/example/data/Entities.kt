package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val customNickname: String,
    val quickShareProfileName: String,
    val peerMeshNodeId: String,
    val isMeshPeer: Boolean, // true = Mesh/Quick Share (never display raw phone numbers), false = Dual-SIM Cellular SMS
    val maskedPhoneHint: String,
    val preferredSimSlot: Int, // 1 = SIM 1, 2 = SIM 2
    val lastMessagePreviewCipher: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val currentTransport: String, // "BLE 5.0", "Wi-Fi Direct 5GHz", "P2P_CLUSTER", "SIM 1 LTE", "SIM 2 5G"
    val hopDistance: Int, // 1..15 hops
    val signalRssiDbm: Int,
    val isBurnerConversation: Boolean = false,
    val burnerContactId: String? = null,
    val burnerExpiresAt: Long? = null,
    val defaultSelfDestructSeconds: Long? = null, // Optional conversation-level self-destruct timer
    val isPinned: Boolean = false, // Gesture-driven swipe right to pin conversation
    val isDecoyRecord: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val isOutgoing: Boolean,
    val bodyCiphertext: String, // Encrypted with Signal Protocol E2EE (X3DH + Double Ratchet AEAD)
    val timestamp: Long,
    val transportMode: String, // "MESH_DTN", "WIFI_DIRECT_5GHZ", "CELLULAR_SMS"
    val ttlHopsRemaining: Int, // Max 15 hops
    val currentHopCount: Int,
    val bloomPacketId: String,
    val simSlotUsed: Int,
    // Signal Protocol E2EE Header Metadata
    val e2eeRatchetGeneration: Int = 1,
    val e2eeMessageNumber: Int = 1,
    val e2eeEphemeralKeyHex: String = "",
    val e2eeMessageKeyDigest: String = "",
    // Quick Swipe-to-Reply Metadata
    val replyToMessageId: String? = null,
    val replyToSenderLabel: String? = null,
    val replyToPreviewText: String? = null,
    // Self-Destructing Message Timer Fields (Deleted from BOTH Sender & Recipient devices upon expiry)
    val selfDestructSeconds: Long? = null, // e.g., 5L (5s), 60L (1m), 3600L (1h), null = Off
    val expiresAtTimestamp: Long? = null,
    // Stealth Mesh-Drop Any-Format File Transfer (*.*) fields
    val attachmentFileName: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentSizeBytes: Long = 0L,
    val swarmTotalChunks: Int = 0,
    val swarmTransferredChunks: Int = 0,
    val blake3RootHash: String? = null,
    val transferSpeedMbps: Int = 0,
    val isPrivacyCameraPhoto: Boolean = false,
    val facesBlurredCount: Int = 0,
    val isScheduledPending: Boolean = false,
    val scheduledSendAt: Long? = null,
    val isDecoyRecord: Boolean = false
)

@Entity(tableName = "burner_contacts")
data class BurnerContactEntity(
    @PrimaryKey val id: String,
    val anonymousBurnerId: String, // e.g., "BRN-7F4A-99C1-E208"
    val aliasNickname: String,
    val shareableTokenUri: String, // e.g., "gcmesh://burner/BRN-7F4A-99C1-E208?spk=..."
    val createdAt: Long,
    val expiresAt: Long?, // Optional expiration timestamp (null = Never expires until manual burn)
    val autoBurnOnExpire: Boolean = true,
    val linkedConversationId: String,
    val signalSafetyNumberSnippet: String,
    val isActive: Boolean = true
)

@Entity(tableName = "tactical_pins")
data class TacticalPinEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "BARRICADE", "FIRST_AID", "SAFE_EXIT", "MESH_RELAY", "SOS_BEACON"
    val gridX: Float, // 0.0f..1.0f relative coordinate on .pmtiles vector canvas
    val gridY: Float,
    val mgrsCoord: String,
    val reporterCallsign: String,
    val ttlHops: Int,
    val timestamp: Long,
    val notesCiphertext: String,
    val isVerifiedBySwarm: Boolean = true
)

@Entity(tableName = "scheduled_sms")
data class ScheduledSmsEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val recipientDisplayName: String,
    val messageCiphertext: String,
    val simSlot: Int,
    val scheduledEpochMillis: Long,
    val status: String // "SCHEDULED_WORKMANAGER", "DISPATCHED", "CANCELLED"
)
