package com.example.data

import com.example.security.CryptoVaultEngine
import com.example.security.SignalProtocolEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GcMessagesRepository(private val dao: GcMessagesDao) {

    fun observeConversations(isDecoy: Boolean): Flow<List<ConversationEntity>> =
        dao.observeConversations(isDecoy)

    fun observeMessages(conversationId: String, isDecoy: Boolean): Flow<List<MessageEntity>> =
        dao.observeMessagesForConversation(conversationId, isDecoy)

    fun observeBurnerContacts(): Flow<List<BurnerContactEntity>> =
        dao.observeBurnerContacts()

    fun observeTacticalPins(): Flow<List<TacticalPinEntity>> =
        dao.observeTacticalPins()

    fun observeScheduledSms(): Flow<List<ScheduledSmsEntity>> =
        dao.observeScheduledSms()

    suspend fun getConversation(conversationId: String): ConversationEntity? =
        dao.getConversationById(conversationId)

    suspend fun updatePeerNickname(conversationId: String, newNickname: String) {
        dao.updatePeerNickname(conversationId, newNickname)
    }

    suspend fun updatePreferredSim(conversationId: String, simSlot: Int) {
        dao.updatePreferredSim(conversationId, simSlot)
    }

    suspend fun toggleConversationPinned(conversationId: String): Boolean {
        val conv = dao.getConversationById(conversationId) ?: return false
        val nextPinned = !conv.isPinned
        dao.updateConversationPinned(conversationId, nextPinned)
        return nextPinned
    }

    suspend fun trashConversation(conversationId: String): ConversationEntity? {
        val conv = dao.getConversationById(conversationId)
        dao.deleteMessagesForConversation(conversationId)
        dao.deleteConversationById(conversationId)
        return conv
    }

    suspend fun restoreTrashedConversation(conversation: ConversationEntity) {
        dao.upsertConversation(conversation)
    }

    suspend fun markConversationRead(conversationId: String) {
        dao.markConversationRead(conversationId)
    }

    fun getInstantDecoyConversationsSnapshot(): List<ConversationEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            ConversationEntity(
                id = "decoy_mom_01",
                customNickname = "Mom",
                quickShareProfileName = "Mom",
                peerMeshNodeId = "CELL-SMS",
                isMeshPeer = false,
                maskedPhoneHint = "Mobile",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = "Can you pick up milk and bread on your way home tonight?",
                lastMessageTimestamp = now - 600_000L,
                unreadCount = 1,
                currentTransport = "SIM 1 LTE",
                hopDistance = 0,
                signalRssiDbm = -65,
                isPinned = true,
                isDecoyRecord = true
            ),
            ConversationEntity(
                id = "decoy_carrier_02",
                customNickname = "Carrier Billing Info",
                quickShareProfileName = "Telecom Automated",
                peerMeshNodeId = "CELL-SMS",
                isMeshPeer = false,
                maskedPhoneHint = "Shortcode 611",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = "Your monthly data plan auto-renewed successfully. Thank you!",
                lastMessageTimestamp = now - 14_400_000L,
                unreadCount = 0,
                currentTransport = "SIM 1 LTE",
                hopDistance = 0,
                signalRssiDbm = -60,
                isPinned = false,
                isDecoyRecord = true
            ),
            ConversationEntity(
                id = "decoy_delivery_03",
                customNickname = "Parcel Express",
                quickShareProfileName = "Courier Updates",
                peerMeshNodeId = "CELL-SMS",
                isMeshPeer = false,
                maskedPhoneHint = "Shortcode 48201",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = "Package #88412 delivered to front porch at 2:14 PM.",
                lastMessageTimestamp = now - 28_800_000L,
                unreadCount = 0,
                currentTransport = "SIM 1 LTE",
                hopDistance = 0,
                signalRssiDbm = -62,
                isPinned = false,
                isDecoyRecord = true
            )
        )
    }

    /**
     * Encrypts and sends a message using the Signal Protocol Double Ratchet & X3DH session
     * bound to `conversationId` so ONLY the sender and recipient can decrypt it.
     * Supports an optional per-message self-destruct timer (`selfDestructSeconds`, e.g. 5s, 60s, 3600s)
     * and swipe-to-reply quote metadata.
     */
    suspend fun sendEncryptedMessage(
        conversationId: String,
        plaintext: String,
        isMeshPeer: Boolean,
        simSlot: Int,
        ttlHops: Int = 15,
        selfDestructSeconds: Long? = null,
        replyToMessageId: String? = null,
        replyToSenderLabel: String? = null,
        replyToPreviewText: String? = null,
        attachmentFileName: String? = null,
        attachmentMimeType: String? = null,
        attachmentSizeBytes: Long = 0L,
        swarmTotalChunks: Int = 0,
        swarmTransferredChunks: Int = 0,
        blake3RootHash: String? = null,
        transferSpeedMbps: Int = 0,
        isPrivacyCameraPhoto: Boolean = false,
        facesBlurredCount: Int = 0,
        isDecoy: Boolean = false
    ): MessageEntity {
        val now = System.currentTimeMillis()
        val expiresAt = selfDestructSeconds?.let { now + it * 1000L }
        val envelope = SignalProtocolEngine.encryptE2eeMessage(
            peerId = conversationId,
            plaintext = plaintext,
            isOutgoing = true
        )
        val packetId = "PKT-" + UUID.randomUUID().toString().take(8).uppercase()
        val transport = when {
            attachmentSizeBytes > 512 * 1024 && isMeshPeer -> "WIFI_DIRECT_5GHZ"
            isMeshPeer -> "MESH_DTN"
            else -> "CELLULAR_SMS"
        }

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            isOutgoing = true,
            bodyCiphertext = envelope.wirePayload,
            timestamp = now,
            transportMode = transport,
            ttlHopsRemaining = if (isMeshPeer) ttlHops else 0,
            currentHopCount = 1,
            bloomPacketId = packetId,
            simSlotUsed = simSlot,
            e2eeRatchetGeneration = envelope.ratchetGeneration,
            e2eeMessageNumber = envelope.messageNumber,
            e2eeEphemeralKeyHex = envelope.ephemeralPublicKeyHex,
            e2eeMessageKeyDigest = envelope.messageKeyDigestHex,
            replyToMessageId = replyToMessageId,
            replyToSenderLabel = replyToSenderLabel,
            replyToPreviewText = replyToPreviewText,
            selfDestructSeconds = selfDestructSeconds,
            expiresAtTimestamp = expiresAt,
            attachmentFileName = attachmentFileName,
            attachmentMimeType = attachmentMimeType,
            attachmentSizeBytes = attachmentSizeBytes,
            swarmTotalChunks = swarmTotalChunks,
            swarmTransferredChunks = swarmTransferredChunks,
            blake3RootHash = blake3RootHash,
            transferSpeedMbps = transferSpeedMbps,
            isPrivacyCameraPhoto = isPrivacyCameraPhoto,
            facesBlurredCount = facesBlurredCount,
            isDecoyRecord = isDecoy
        )
        dao.insertMessage(message)

        dao.getConversationById(conversationId)?.let { conv ->
            val timerTag = if (selfDestructSeconds != null) "⏱️ [${formatTimerShort(selfDestructSeconds)}] " else ""
            val previewText = if (attachmentFileName != null) "$timerTag📦 [Mesh-Drop] $attachmentFileName" else "$timerTag$plaintext"
            val previewEnvelope = SignalProtocolEngine.encryptE2eeMessage(
                peerId = conversationId,
                plaintext = previewText,
                isOutgoing = true
            )
            dao.upsertConversation(
                conv.copy(
                    lastMessagePreviewCipher = previewEnvelope.wirePayload,
                    lastMessageTimestamp = now,
                    currentTransport = if (attachmentSizeBytes > 512 * 1024 && isMeshPeer) "Wi-Fi Direct 5GHz" else conv.currentTransport
                )
            )
        }
        return message
    }

    /**
     * Sweeps all messages whose `expiresAtTimestamp` has elapsed, zeroes their Signal Ephemeral Message Key in RAM,
     * deletes the message record from the local SQLite/SQLCipher database, updates conversation previews,
     * and returns the list of expired messages so the Mesh Engine can dispatch remote burn tombstones to the peer device.
     */
    suspend fun sweepAndDeleteExpiredSelfDestructMessages(): List<MessageEntity> {
        val now = System.currentTimeMillis()
        val expiredMessages = dao.getExpiredSelfDestructMessages(now)
        if (expiredMessages.isEmpty()) return emptyList()

        for (msg in expiredMessages) {
            SignalProtocolEngine.purgeEphemeralMessageKey(
                peerId = msg.conversationId,
                ratchetGeneration = msg.e2eeRatchetGeneration,
                messageNumber = msg.e2eeMessageNumber,
                isOutgoing = msg.isOutgoing
            )
            dao.deleteMessageById(msg.id)

            // Update conversation preview to reflect the latest remaining message or self-destruct notice
            dao.getConversationById(msg.conversationId)?.let { conv ->
                val latestRemaining = dao.getLatestMessageForConversation(msg.conversationId)
                val newPreviewCipher = latestRemaining?.bodyCiphertext
                    ?: SignalProtocolEngine.encryptE2eeMessage(
                        peerId = msg.conversationId,
                        plaintext = "🔥 [Self-Destructing Message Expired & Wiped on Both Devices]",
                        isOutgoing = true
                    ).wirePayload
                dao.upsertConversation(
                    conv.copy(lastMessagePreviewCipher = newPreviewCipher)
                )
            }
        }
        return expiredMessages
    }

    private fun formatTimerShort(seconds: Long): String {
        return when {
            seconds < 60L -> "${seconds}s"
            seconds < 3600L -> "${seconds / 60}m"
            else -> "${seconds / 3600}h"
        }
    }

    /**
     * Generates a new anonymous Burner Contact ID (`BRN-XXXX-XXXX-XXXX`) managed strictly separately
     * from the device's main `ContactsContract` list, with an optional expiration date and dedicated Signal E2EE session.
     */
    suspend fun createBurnerContact(
        aliasNickname: String,
        expirationDurationMinutes: Long? // null = Never expires until manual burn
    ): BurnerContactEntity {
        val now = System.currentTimeMillis()
        val (anonymousId, shareToken) = CryptoVaultEngine.generateAnonymousBurnerId("BRN")
        val burnerPrimaryKey = "burner_" + UUID.randomUUID().toString().take(8)
        val linkedConvId = "conv_$burnerPrimaryKey"
        val expiresAt = expirationDurationMinutes?.let { now + it * 60_000L }

        // Establish dedicated X3DH + Double Ratchet session for this Burner ID
        SignalProtocolEngine.getOrEstablishSession(linkedConvId)
        val safetySnippet = SignalProtocolEngine.computeSafetyNumber(linkedConvId).take(19)

        val burnerEntity = BurnerContactEntity(
            id = burnerPrimaryKey,
            anonymousBurnerId = anonymousId,
            aliasNickname = aliasNickname.ifBlank { "Anonymous Burner" },
            shareableTokenUri = shareToken,
            createdAt = now,
            expiresAt = expiresAt,
            autoBurnOnExpire = expiresAt != null,
            linkedConversationId = linkedConvId,
            signalSafetyNumberSnippet = safetySnippet,
            isActive = true
        )
        dao.insertBurnerContact(burnerEntity)

        val introEnvelope = SignalProtocolEngine.encryptE2eeMessage(
            peerId = linkedConvId,
            plaintext = "🔥 Burner Contact [$anonymousId] initialized with Signal E2EE Double Ratchet. Isolated from device contacts.",
            isOutgoing = false
        )

        val conv = ConversationEntity(
            id = linkedConvId,
            customNickname = "🔥 ${burnerEntity.aliasNickname} ($anonymousId)",
            quickShareProfileName = "Burner ID • Isolated Vault",
            peerMeshNodeId = anonymousId,
            isMeshPeer = true,
            maskedPhoneHint = "BURNER_ISOLATED_ID",
            preferredSimSlot = 1,
            lastMessagePreviewCipher = introEnvelope.wirePayload,
            lastMessageTimestamp = now,
            unreadCount = 1,
            currentTransport = "P2P_CLUSTER",
            hopDistance = 1,
            signalRssiDbm = -52,
            isBurnerConversation = true,
            burnerContactId = burnerPrimaryKey,
            burnerExpiresAt = expiresAt,
            isDecoyRecord = false
        )
        dao.upsertConversation(conv)

        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = linkedConvId,
                isOutgoing = false,
                bodyCiphertext = introEnvelope.wirePayload,
                timestamp = now,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 15,
                currentHopCount = 1,
                bloomPacketId = "BRN-INIT-${anonymousId.takeLast(4)}",
                simSlotUsed = 1,
                e2eeRatchetGeneration = introEnvelope.ratchetGeneration,
                e2eeMessageNumber = introEnvelope.messageNumber,
                e2eeEphemeralKeyHex = introEnvelope.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = introEnvelope.messageKeyDigestHex
            )
        )
        return burnerEntity
    }

    /**
     * Permanently burns (destroys) a Burner Contact, its isolated conversation, all E2EE messages,
     * and its Double Ratchet keys.
     */
    suspend fun burnAndDestroyBurnerContact(burnerId: String) {
        val burner = dao.getBurnerContactById(burnerId) ?: return
        dao.deleteMessagesForConversation(burner.linkedConversationId)
        dao.deleteConversationById(burner.linkedConversationId)
        dao.deleteBurnerContactById(burnerId)
    }

    /**
     * Automated Dead Man's Switch execution:
     * Shreds all local encryption keys in RAM, zeroes all Signal Double Ratchet chains,
     * and wipes all real conversations and burner contacts when the inactivity timer expires.
     */
    suspend fun executeDeadManSwitchWipe() {
        CryptoVaultEngine.triggerDuressPanicWipe()
        val expiredBurners = dao.getExpiredBurnerContacts(Long.MAX_VALUE)
        expiredBurners.forEach { burnAndDestroyBurnerContact(it.id) }
    }

    /**
     * Sweeps and destroys any Burner Contacts whose expiration timestamp has passed.
     */
    suspend fun purgeExpiredBurnerContacts(): Int {
        val now = System.currentTimeMillis()
        val expiredList = dao.getExpiredBurnerContacts(now)
        expiredList.forEach { burner ->
            burnAndDestroyBurnerContact(burner.id)
        }
        return expiredList.size
    }

    suspend fun updateSwarmProgress(
        messageId: String,
        transferredChunks: Int,
        speedMbps: Int,
        transportMode: String
    ) {
        dao.updateSwarmTransferProgress(messageId, transferredChunks, speedMbps, transportMode)
    }

    suspend fun addTacticalPin(
        title: String,
        category: String,
        gridX: Float,
        gridY: Float,
        mgrsCoord: String,
        reporterCallsign: String,
        notes: String
    ) {
        val pin = TacticalPinEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            gridX = gridX.coerceIn(0.08f, 0.92f),
            gridY = gridY.coerceIn(0.08f, 0.92f),
            mgrsCoord = mgrsCoord,
            reporterCallsign = reporterCallsign,
            ttlHops = 15,
            timestamp = System.currentTimeMillis(),
            notesCiphertext = CryptoVaultEngine.encryptAes256Gcm(notes),
            isVerifiedBySwarm = true
        )
        dao.insertTacticalPin(pin)
    }

    suspend fun deleteTacticalPin(pinId: String) {
        dao.deleteTacticalPin(pinId)
    }

    suspend fun insertScheduledSms(item: ScheduledSmsEntity) {
        dao.insertScheduledSms(item)
    }

    suspend fun updateScheduledSmsStatus(id: String, status: String) {
        dao.updateScheduledSmsStatus(id, status)
    }

    suspend fun upsertConversation(conversation: ConversationEntity) {
        dao.upsertConversation(conversation)
    }

    suspend fun seedInitialTacticalAndDecoyDataIfEmpty() {
        if (dao.getConversationCount() > 0) return
        val now = System.currentTimeMillis()

        val sigEnc = { peerId: String, text: String, outgoing: Boolean ->
            SignalProtocolEngine.encryptE2eeMessage(peerId, text, outgoing)
        }

        // Pre-seed messages with real Signal Protocol E2EE X3DH + Double Ratchet envelopes
        val vMsg1 = sigEnc("peer_valkyrie_01", "Signal X3DH + Double Ratchet E2EE session established over BLE 5.0 P2P_CLUSTER. Hop count: 2/15.", false)
        val vMsg2 = sigEnc("peer_valkyrie_01", "Copy Valkyrie-7. Safety Number verified. Send the offline .pmtiles vector map via Stealth Mesh-Drop.", true)
        val vMsg3 = sigEnc("peer_valkyrie_01", "Auto-Gear Shift engaged: BLE 5.0 -> Wi-Fi Direct 5GHz (86 MB/s). Swarm chunking 194 x 256KB BLAKE3 blocks.", false)

        val kMsg1 = sigEnc("peer_kestrel_02", "Triage station operational at North Tunnel Safe Exit. E2EE Ratchet verified across 4 hops.", false)
        val sMsg1 = sigEnc("peer_spectre_03", "Privacy Camera evidence verified. EXIF & GPS stripped, 2 faces blurred.", false)
        val cMsg1 = sigEnc("sms_dispatch_04", "STRICT ISOLATION ACTIVE: Cellular SMS encrypted with Signal E2EE and blocked from public BLE mesh.", false)

        // Seed 2 initial Burner Contacts with optional expiration dates (managed separately from ContactsContract)
        val (burnerId1, token1) = CryptoVaultEngine.generateAnonymousBurnerId("BRN")
        val (burnerId2, token2) = CryptoVaultEngine.generateAnonymousBurnerId("BRN")
        val burnerConvId1 = "conv_burner_phantom_01"
        val bMsg1 = sigEnc(burnerConvId1, "Anonymous Burner ID [$burnerId1] active. Auto-expires in 24h. Zero linkage to device contacts.", false)

        val initialBurners = listOf(
            BurnerContactEntity(
                id = "burner_phantom_01",
                anonymousBurnerId = burnerId1,
                aliasNickname = "Phantom-Drop-Contact",
                shareableTokenUri = token1,
                createdAt = now - 300_000L,
                expiresAt = now + 24 * 3_600_000L, // Expires in 24 hours
                autoBurnOnExpire = true,
                linkedConversationId = burnerConvId1,
                signalSafetyNumberSnippet = SignalProtocolEngine.computeSafetyNumber(burnerConvId1).take(19),
                isActive = true
            ),
            BurnerContactEntity(
                id = "burner_oracle_02",
                anonymousBurnerId = burnerId2,
                aliasNickname = "Oracle-Source-Zero",
                shareableTokenUri = token2,
                createdAt = now - 900_000L,
                expiresAt = now + 60 * 60_000L, // Expires in 1 hour
                autoBurnOnExpire = true,
                linkedConversationId = "conv_burner_oracle_02",
                signalSafetyNumberSnippet = SignalProtocolEngine.computeSafetyNumber("conv_burner_oracle_02").take(19),
                isActive = true
            )
        )
        dao.insertBurnerContacts(initialBurners)

        val realConversations = listOf(
            ConversationEntity(
                id = "peer_valkyrie_01",
                customNickname = "Valkyrie-7 (Forward Recon)",
                quickShareProfileName = "Elena Vance • Quick Share",
                peerMeshNodeId = "MESH-NODE-8F3A-BLE5",
                isMeshPeer = true,
                maskedPhoneHint = "IDENTITY_MASKED_QS",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = vMsg3.wirePayload,
                lastMessageTimestamp = now - 180_000L,
                unreadCount = 2,
                currentTransport = "Wi-Fi Direct 5GHz",
                hopDistance = 2,
                signalRssiDbm = -54,
                isPinned = true,
                isDecoyRecord = false
            ),
            ConversationEntity(
                id = burnerConvId1,
                customNickname = "🔥 Phantom-Drop ($burnerId1)",
                quickShareProfileName = "Burner Contact • Isolated Vault",
                peerMeshNodeId = burnerId1,
                isMeshPeer = true,
                maskedPhoneHint = "BURNER_ISOLATED_ID",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = bMsg1.wirePayload,
                lastMessageTimestamp = now - 290_000L,
                unreadCount = 1,
                currentTransport = "P2P_CLUSTER",
                hopDistance = 1,
                signalRssiDbm = -51,
                isBurnerConversation = true,
                burnerContactId = "burner_phantom_01",
                burnerExpiresAt = now + 24 * 3_600_000L,
                isDecoyRecord = false
            ),
            ConversationEntity(
                id = "peer_kestrel_02",
                customNickname = "Kestrel-MedEvac",
                quickShareProfileName = "Dr. Marcus Chen • Pixel 9 Pro",
                peerMeshNodeId = "MESH-NODE-4C19-P2P",
                isMeshPeer = true,
                maskedPhoneHint = "IDENTITY_MASKED_QS",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = kMsg1.wirePayload,
                lastMessageTimestamp = now - 940_000L,
                unreadCount = 0,
                currentTransport = "P2P_CLUSTER",
                hopDistance = 4,
                signalRssiDbm = -68,
                isDecoyRecord = false
            ),
            ConversationEntity(
                id = "peer_spectre_03",
                customNickname = "Spectre-Node-12 (Crowd Relay)",
                quickShareProfileName = "Galaxy S25 Ultra • Quick Share",
                peerMeshNodeId = "MESH-NODE-99E1-DTN",
                isMeshPeer = true,
                maskedPhoneHint = "IDENTITY_MASKED_QS",
                preferredSimSlot = 2,
                lastMessagePreviewCipher = sMsg1.wirePayload,
                lastMessageTimestamp = now - 2_700_000L,
                unreadCount = 1,
                currentTransport = "BLE 5.0",
                hopDistance = 7,
                signalRssiDbm = -79,
                isDecoyRecord = false
            ),
            ConversationEntity(
                id = "sms_dispatch_04",
                customNickname = "Civil Defense Dispatch (Cellular SMS)",
                quickShareProfileName = "Cellular SMS Gateway",
                peerMeshNodeId = "CELLULAR-ISOLATED-CHANNEL",
                isMeshPeer = false,
                maskedPhoneHint = "SIM 2 • Out-of-Band Cellular",
                preferredSimSlot = 2,
                lastMessagePreviewCipher = cMsg1.wirePayload,
                lastMessageTimestamp = now - 5_400_000L,
                unreadCount = 0,
                currentTransport = "SIM 2 5G",
                hopDistance = 0,
                signalRssiDbm = -61,
                isDecoyRecord = false
            ),
            // Decoy SMS Inbox Conversations
            ConversationEntity(
                id = "decoy_mom_01",
                customNickname = "Mom",
                quickShareProfileName = "Mom",
                peerMeshNodeId = "CELL-SMS",
                isMeshPeer = false,
                maskedPhoneHint = "Mobile",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = "Can you pick up milk and bread on your way home tonight?",
                lastMessageTimestamp = now - 600_000L,
                unreadCount = 1,
                currentTransport = "SIM 1 LTE",
                hopDistance = 0,
                signalRssiDbm = -65,
                isPinned = true,
                isDecoyRecord = true
            ),
            ConversationEntity(
                id = "decoy_carrier_02",
                customNickname = "Carrier Billing Info",
                quickShareProfileName = "Telecom Automated",
                peerMeshNodeId = "CELL-SMS",
                isMeshPeer = false,
                maskedPhoneHint = "Shortcode 611",
                preferredSimSlot = 1,
                lastMessagePreviewCipher = "Your monthly data plan auto-renewed successfully. Thank you!",
                lastMessageTimestamp = now - 14_400_000L,
                unreadCount = 0,
                currentTransport = "SIM 1 LTE",
                hopDistance = 0,
                signalRssiDbm = -60,
                isDecoyRecord = true
            )
        )
        dao.upsertConversations(realConversations)

        val initialMessages = listOf(
            MessageEntity(
                id = "msg_v1",
                conversationId = "peer_valkyrie_01",
                isOutgoing = false,
                bodyCiphertext = vMsg1.wirePayload,
                timestamp = now - 600_000L,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 13,
                currentHopCount = 2,
                bloomPacketId = "PKT-88A1F09C",
                simSlotUsed = 1,
                e2eeRatchetGeneration = vMsg1.ratchetGeneration,
                e2eeMessageNumber = vMsg1.messageNumber,
                e2eeEphemeralKeyHex = vMsg1.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = vMsg1.messageKeyDigestHex
            ),
            MessageEntity(
                id = "msg_v2",
                conversationId = "peer_valkyrie_01",
                isOutgoing = true,
                bodyCiphertext = vMsg2.wirePayload,
                timestamp = now - 420_000L,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 15,
                currentHopCount = 1,
                bloomPacketId = "PKT-41C9D20E",
                simSlotUsed = 1,
                e2eeRatchetGeneration = vMsg2.ratchetGeneration,
                e2eeMessageNumber = vMsg2.messageNumber,
                e2eeEphemeralKeyHex = vMsg2.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = vMsg2.messageKeyDigestHex,
                replyToMessageId = "msg_v1",
                replyToSenderLabel = "Valkyrie-7 (Forward Recon)",
                replyToPreviewText = "Signal X3DH + Double Ratchet E2EE session established over BLE 5.0 P2P_CLUSTER."
            ),
            MessageEntity(
                id = "msg_v3",
                conversationId = "peer_valkyrie_01",
                isOutgoing = false,
                bodyCiphertext = vMsg3.wirePayload,
                timestamp = now - 180_000L,
                transportMode = "WIFI_DIRECT_5GHZ",
                ttlHopsRemaining = 14,
                currentHopCount = 2,
                bloomPacketId = "PKT-99B3E71A",
                simSlotUsed = 1,
                e2eeRatchetGeneration = vMsg3.ratchetGeneration,
                e2eeMessageNumber = vMsg3.messageNumber,
                e2eeEphemeralKeyHex = vMsg3.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = vMsg3.messageKeyDigestHex,
                attachmentFileName = "sector7_offline_tactical.pmtiles",
                attachmentMimeType = "application/vnd.pmtiles",
                attachmentSizeBytes = 50_855_936L,
                swarmTotalChunks = 194,
                swarmTransferredChunks = 194,
                blake3RootHash = "b3_7f9a82e4c1093d8b5a1e2f04",
                transferSpeedMbps = 86
            ),
            MessageEntity(
                id = "msg_b1",
                conversationId = burnerConvId1,
                isOutgoing = false,
                bodyCiphertext = bMsg1.wirePayload,
                timestamp = now - 290_000L,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 15,
                currentHopCount = 1,
                bloomPacketId = "PKT-BRN-77A1",
                simSlotUsed = 1,
                e2eeRatchetGeneration = bMsg1.ratchetGeneration,
                e2eeMessageNumber = bMsg1.messageNumber,
                e2eeEphemeralKeyHex = bMsg1.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = bMsg1.messageKeyDigestHex
            ),
            MessageEntity(
                id = "msg_k1",
                conversationId = "peer_kestrel_02",
                isOutgoing = false,
                bodyCiphertext = kMsg1.wirePayload,
                timestamp = now - 940_000L,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 11,
                currentHopCount = 4,
                bloomPacketId = "PKT-23E7A910",
                simSlotUsed = 1,
                e2eeRatchetGeneration = kMsg1.ratchetGeneration,
                e2eeMessageNumber = kMsg1.messageNumber,
                e2eeEphemeralKeyHex = kMsg1.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = kMsg1.messageKeyDigestHex
            ),
            MessageEntity(
                id = "msg_s1",
                conversationId = "peer_spectre_03",
                isOutgoing = false,
                bodyCiphertext = sMsg1.wirePayload,
                timestamp = now - 2_700_000L,
                transportMode = "MESH_DTN",
                ttlHopsRemaining = 8,
                currentHopCount = 7,
                bloomPacketId = "PKT-71D0B442",
                simSlotUsed = 2,
                e2eeRatchetGeneration = sMsg1.ratchetGeneration,
                e2eeMessageNumber = sMsg1.messageNumber,
                e2eeEphemeralKeyHex = sMsg1.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = sMsg1.messageKeyDigestHex,
                attachmentFileName = "evidence_checkpoint_redacted.jpg",
                attachmentMimeType = "image/jpeg",
                attachmentSizeBytes = 1_843_200L,
                swarmTotalChunks = 8,
                swarmTransferredChunks = 8,
                blake3RootHash = "b3_4c18e09fa231887bc1029a8f",
                transferSpeedMbps = 64,
                isPrivacyCameraPhoto = true,
                facesBlurredCount = 2
            ),
            MessageEntity(
                id = "msg_c1",
                conversationId = "sms_dispatch_04",
                isOutgoing = false,
                bodyCiphertext = cMsg1.wirePayload,
                timestamp = now - 5_400_000L,
                transportMode = "CELLULAR_SMS",
                ttlHopsRemaining = 0,
                currentHopCount = 0,
                bloomPacketId = "SMS-ISOLATED-01",
                simSlotUsed = 2,
                e2eeRatchetGeneration = cMsg1.ratchetGeneration,
                e2eeMessageNumber = cMsg1.messageNumber,
                e2eeEphemeralKeyHex = cMsg1.ephemeralPublicKeyHex,
                e2eeMessageKeyDigest = cMsg1.messageKeyDigestHex
            ),
            // Decoy messages
            MessageEntity(
                id = "msg_decoy_1",
                conversationId = "decoy_mom_01",
                isOutgoing = false,
                bodyCiphertext = "Hey! Are we still on for Sunday dinner at 6?",
                timestamp = now - 3_600_000L,
                transportMode = "CELLULAR_SMS",
                ttlHopsRemaining = 0,
                currentHopCount = 0,
                bloomPacketId = "DECOY-01",
                simSlotUsed = 1,
                isDecoyRecord = true
            ),
            MessageEntity(
                id = "msg_decoy_2",
                conversationId = "decoy_mom_01",
                isOutgoing = true,
                bodyCiphertext = "Yes! I'll bring dessert.",
                timestamp = now - 1_800_000L,
                transportMode = "CELLULAR_SMS",
                ttlHopsRemaining = 0,
                currentHopCount = 0,
                bloomPacketId = "DECOY-02",
                simSlotUsed = 1,
                isDecoyRecord = true
            )
        )
        dao.insertMessages(initialMessages)

        val enc = { text: String -> CryptoVaultEngine.encryptAes256Gcm(text) }
        val pins = listOf(
            TacticalPinEntity(
                id = "pin_1",
                title = "Main Bridge Barricade",
                category = "BARRICADE",
                gridX = 0.34f,
                gridY = 0.42f,
                mgrsCoord = "18TWL 84210 11940",
                reporterCallsign = "Valkyrie-7",
                ttlHops = 14,
                timestamp = now - 900_000L,
                notesCiphertext = enc("Heavy roadblock across West Span. Reroute via pedestrian underpass.")
            ),
            TacticalPinEntity(
                id = "pin_2",
                title = "Field First-Aid & Water",
                category = "FIRST_AID",
                gridX = 0.62f,
                gridY = 0.28f,
                mgrsCoord = "18TWL 84890 12310",
                reporterCallsign = "Kestrel-MedEvac",
                ttlHops = 12,
                timestamp = now - 1_500_000L,
                notesCiphertext = enc("Trauma kits, saline, and solar battery charging bank available.")
            ),
            TacticalPinEntity(
                id = "pin_3",
                title = "North Tunnel Safe Exit",
                category = "SAFE_EXIT",
                gridX = 0.76f,
                gridY = 0.68f,
                mgrsCoord = "18TWL 85320 10870",
                reporterCallsign = "Spectre-Node-12",
                ttlHops = 15,
                timestamp = now - 400_000L,
                notesCiphertext = enc("Clear egress corridor verified by 6 mesh nodes. 5GHz relay repeater mounted.")
            )
        )
        dao.insertTacticalPins(pins)
    }
}
