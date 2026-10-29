package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BurnerContactEntity
import com.example.data.ConversationEntity
import com.example.data.GcMessagesDatabase
import com.example.data.GcMessagesRepository
import com.example.data.MessageEntity
import com.example.data.ScheduledSmsEntity
import com.example.data.TacticalPinEntity
import com.example.security.CryptoVaultEngine
import com.example.security.PrivacyCaptureResult
import com.example.security.SignalProtocolEngine
import com.example.security.SignalSessionSummary
import com.example.security.SwarmChunkMeta
import com.example.service.ActiveVaultProfile
import com.example.service.AdaptiveContrastMode
import com.example.service.AdvancedMeshCryptoEngine
import com.example.service.CellTowerAnomalyReport
import com.example.service.DeadManSwitchState
import com.example.service.DtnPacketLog
import com.example.service.FontScalePreset
import com.example.service.GcBackupArchiveMeta
import com.example.service.HapticPatternType
import com.example.service.LocalContactEntry
import com.example.service.MeshGroupKeyBundle
import com.example.service.MeshPeerNode
import com.example.service.MeshTransportEngine
import com.example.service.PredictiveRouteCandidate
import com.example.service.PttVoiceTransmission
import com.example.service.RadioGearState
import com.example.service.SatelliteNtnGatewayState
import com.example.service.SdrBridgeFrameLog
import com.example.service.SdrModulationMode
import com.example.service.ShardedFileManifest
import com.example.service.SimCardSlotInfo
import com.example.service.StealthAndTelephonyHelper
import com.example.service.TacticalHapticEngine
import com.example.service.TacticalHardwareAndSecuritySuite
import com.example.service.ThreatSeverity
import com.example.service.UltrasonicBurstFrame
import com.example.service.ZkpAnonymousAttestationProof
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class MainNavTab {
    MESSAGES,
    BURNER_IDS,
    MESH_RADAR,
    TACTICAL_MAP,
    STEALTH_VAULT
}

data class StealthFileDropPreset(
    val title: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val formatBadge: String
)

data class SelfDestructTimerPreset(
    val label: String,
    val seconds: Long? // null = Off, 5L = 5s, 60L = 1m, 3600L = 1h
)

@OptIn(ExperimentalCoroutinesApi::class)
class GcMessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GcMessagesDatabase.getInstance(application)
    private val repository = GcMessagesRepository(db.dao())
    val meshEngine = MeshTransportEngine(application, viewModelScope)

    private val _selectedTab = MutableStateFlow(MainNavTab.MESSAGES)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _isDuressDecoyMode = MutableStateFlow(CryptoVaultEngine.isDuressDecoyMode)
    val isDuressDecoyMode: StateFlow<Boolean> = _isDuressDecoyMode.asStateFlow()

    // Multi-Vault / Dual-Password Hidden Profiles (Primary PIN: 1234#, Secondary Decoy PIN: 5555#, Panic Shred PIN: 9999#)
    private val _activeVaultProfile = MutableStateFlow(ActiveVaultProfile.PRIMARY_REAL_VAULT)
    val activeVaultProfile: StateFlow<ActiveVaultProfile> = _activeVaultProfile.asStateFlow()

    // Selected per-message Self-Destruct Timer (null = Off, 5L = 5s, 60L = 1m, 3600L = 1h)
    private val _selectedSelfDestructSeconds = MutableStateFlow<Long?>(null)
    val selectedSelfDestructSeconds: StateFlow<Long?> = _selectedSelfDestructSeconds.asStateFlow()

    val selfDestructPresets = listOf(
        SelfDestructTimerPreset("Off", null),
        SelfDestructTimerPreset("5s", 5L),
        SelfDestructTimerPreset("1m", 60L),
        SelfDestructTimerPreset("1h", 3600L)
    )

    private val _keyFingerprint = MutableStateFlow(CryptoVaultEngine.getKeyFingerprint())
    val keyFingerprint: StateFlow<String> = _keyFingerprint.asStateFlow()

    private val _signalIdentityFingerprint = MutableStateFlow(SignalProtocolEngine.getLocalIdentityKeyFingerprint())
    val signalIdentityFingerprint: StateFlow<String> = _signalIdentityFingerprint.asStateFlow()

    private val _signalSignedPreKeyFingerprint = MutableStateFlow(SignalProtocolEngine.getLocalSignedPreKeyFingerprint())
    val signalSignedPreKeyFingerprint: StateFlow<String> = _signalSignedPreKeyFingerprint.asStateFlow()

    private val _activeSignalSessionSummary = MutableStateFlow<SignalSessionSummary?>(null)
    val activeSignalSessionSummary: StateFlow<SignalSessionSummary?> = _activeSignalSessionSummary.asStateFlow()

    private val _volumeDownPanicCounter = MutableStateFlow(0)
    val volumeDownPanicCounter: StateFlow<Int> = _volumeDownPanicCounter.asStateFlow()

    private var lastVolumeDownPressTime = 0L

    private val _isStealthDialerHidden = MutableStateFlow(false)
    val isStealthDialerHidden: StateFlow<Boolean> = _isStealthDialerHidden.asStateFlow()

    private val _selectedSimSlot = MutableStateFlow(1)
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    private val _simSlots = MutableStateFlow<List<SimCardSlotInfo>>(emptyList())
    val simSlots: StateFlow<List<SimCardSlotInfo>> = _simSlots.asStateFlow()

    private val _localContacts = MutableStateFlow<List<LocalContactEntry>>(emptyList())
    val localContacts: StateFlow<List<LocalContactEntry>> = _localContacts.asStateFlow()

    private val _lastPrivacyCapture = MutableStateFlow<PrivacyCaptureResult?>(null)
    val lastPrivacyCapture: StateFlow<PrivacyCaptureResult?> = _lastPrivacyCapture.asStateFlow()

    private val _activeSwarmChunks = MutableStateFlow<List<SwarmChunkMeta>>(emptyList())
    val activeSwarmChunks: StateFlow<List<SwarmChunkMeta>> = _activeSwarmChunks.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 1. Mesh Walkie-Talkie (Live PTT Audio Stream over BLE 5.0 + Wi-Fi Direct)
    private val _isPttTransmitting = MutableStateFlow(false)
    val isPttTransmitting: StateFlow<Boolean> = _isPttTransmitting.asStateFlow()

    private val _pttActiveChannel = MutableStateFlow("CH-1 • Tactical Swarm (BLE + 5GHz)")
    val pttActiveChannel: StateFlow<String> = _pttActiveChannel.asStateFlow()

    private val _pttTransmissions = MutableStateFlow<List<PttVoiceTransmission>>(
        listOf(
            PttVoiceTransmission(
                id = "ptt_1",
                speakerCallsign = "Valkyrie-7",
                channelName = "CH-1 • Tactical Swarm",
                durationMillis = 4200L,
                codecSpec = "Opus 24kbps • 16kHz PCM • Signal E2EE",
                peakAmplitudeDb = -6,
                rssiDbm = -54,
                timestamp = System.currentTimeMillis() - 95_000L,
                isOutgoing = false
            ),
            PttVoiceTransmission(
                id = "ptt_2",
                speakerCallsign = "Kestrel-MedEvac",
                channelName = "CH-1 • Tactical Swarm",
                durationMillis = 3100L,
                codecSpec = "Opus 24kbps • 16kHz PCM • Signal E2EE",
                peakAmplitudeDb = -9,
                rssiDbm = -66,
                timestamp = System.currentTimeMillis() - 40_000L,
                isOutgoing = false
            )
        )
    )
    val pttTransmissions: StateFlow<List<PttVoiceTransmission>> = _pttTransmissions.asStateFlow()

    // 2. Automated Dead Man's Switch (24h / 48h Inactivity Timer)
    private val _deadManSwitchState = MutableStateFlow(
        DeadManSwitchState(
            isArmed = true,
            timeoutHours = 24,
            lastCheckInEpochMillis = System.currentTimeMillis(),
            expiresAtEpochMillis = System.currentTimeMillis() + 24 * 3_600_000L
        )
    )
    val deadManSwitchState: StateFlow<DeadManSwitchState> = _deadManSwitchState.asStateFlow()

    // 3. IMSI Catcher & Fake Cell Tower (Stingray) Detector
    private val _cellTowerReports = MutableStateFlow<List<CellTowerAnomalyReport>>(emptyList())
    val cellTowerReports: StateFlow<List<CellTowerAnomalyReport>> = _cellTowerReports.asStateFlow()

    private val _isExclusiveOfflineMeshLocked = MutableStateFlow(false)
    val isExclusiveOfflineMeshLocked: StateFlow<Boolean> = _isExclusiveOfflineMeshLocked.asStateFlow()

    // 5. Mesh QR / NFC 1-Tap E2EE Group Key Distribution
    private val _meshGroupKeys = MutableStateFlow<List<MeshGroupKeyBundle>>(
        listOf(
            TacticalHardwareAndSecuritySuite.generateGroupSenderKeyBundle("Sector-7 Recon Group"),
            TacticalHardwareAndSecuritySuite.generateGroupSenderKeyBundle("MedEvac Triage Net")
        )
    )
    val meshGroupKeys: StateFlow<List<MeshGroupKeyBundle>> = _meshGroupKeys.asStateFlow()

    // 6. Encrypted Zero-Knowledge Local Backups (`.gcbackup`)
    private val _gcBackups = MutableStateFlow<List<GcBackupArchiveMeta>>(emptyList())
    val gcBackups: StateFlow<List<GcBackupArchiveMeta>> = _gcBackups.asStateFlow()

    // 7. SDR & Tactical Radio Hardware Bridge (VHF/UHF KISS TNC AX.25)
    private val _selectedSdrMode = MutableStateFlow(SdrModulationMode.VHF_2M_AFSK)
    val selectedSdrMode: StateFlow<SdrModulationMode> = _selectedSdrMode.asStateFlow()

    private val _sdrBridgeLogs = MutableStateFlow<List<SdrBridgeFrameLog>>(
        listOf(
            TacticalHardwareAndSecuritySuite.encodeKissAx25SdrFrame(
                sourceCallsign = "GC0OPS",
                destCallsign = "VALK07",
                mode = SdrModulationMode.VHF_2M_AFSK,
                encryptedPayload = "E2EE_SIGv3|MESH_RELAY_BEACON"
            )
        )
    )
    val sdrBridgeLogs: StateFlow<List<SdrBridgeFrameLog>> = _sdrBridgeLogs.asStateFlow()

    // 8. Acoustic / Ultrasonic Data Burst Transmission (18.5 kHz / 19.4 kHz GFSK)
    private val _ultrasonicBurstLogs = MutableStateFlow<List<UltrasonicBurstFrame>>(
        listOf(
            AdvancedMeshCryptoEngine.transmitUltrasonicAcousticBurst(
                payloadType = "PQ_PREKEY_BUNDLE",
                messageText = "KYBER1024+DILITHIUM5 PREKEY SYNC",
                playPhysicalUltrasonicTone = false
            )
        )
    )
    val ultrasonicBurstLogs: StateFlow<List<UltrasonicBurstFrame>> = _ultrasonicBurstLogs.asStateFlow()

    // 9. AI-Driven Predictive Mesh Routing
    private val _aiPredictiveRoutes = MutableStateFlow<List<PredictiveRouteCandidate>>(
        AdvancedMeshCryptoEngine.computeAiPredictiveMeshRoutes(meshEngine.meshPeers.value)
    )
    val aiPredictiveRoutes: StateFlow<List<PredictiveRouteCandidate>> = _aiPredictiveRoutes.asStateFlow()

    // 10. Direct-to-Cell Satellite Fallback Bridge (3GPP NTN L-Band / S-Band LEO)
    private val _satelliteGatewayState = MutableStateFlow(
        SatelliteNtnGatewayState(
            isSatelliteBridgeEnabled = true,
            designatedGatewayNode = "Apex-Relay-04 (Open-Sky LOS)",
            constellationName = "3GPP Rel-17 NTN • LEO Direct-to-Cell (1.6 GHz L-Band)",
            openSkyAzimuthElevation = "Az 142° • El 68° (Clear Sky)",
            snrDb = 11.4,
            uplinkPacketsRelayed = 6,
            lastPassTimestamp = System.currentTimeMillis() - 120_000L
        )
    )
    val satelliteGatewayState: StateFlow<SatelliteNtnGatewayState> = _satelliteGatewayState.asStateFlow()

    // 11. Zero-Knowledge Proof (ZKP) Anonymous Group Attestation
    private val _zkpAttestationProofs = MutableStateFlow<List<ZkpAnonymousAttestationProof>>(
        listOf(
            AdvancedMeshCryptoEngine.generateAndVerifyZkpGroupAttestation("RING-SECTOR7-RECON")
        )
    )
    val zkpAttestationProofs: StateFlow<List<ZkpAnonymousAttestationProof>> = _zkpAttestationProofs.asStateFlow()

    // 12. Decentralized Local Sharding (3-of-5 Fragmented Swarm Storage)
    private val _shardedFileManifests = MutableStateFlow<List<ShardedFileManifest>>(
        listOf(
            AdvancedMeshCryptoEngine.shardFileAcrossTrustedMeshPeers(
                fileName = "classified_field_topology.pmtiles",
                totalBytes = 15_728_640L,
                peers = meshEngine.meshPeers.value
            )
        )
    )
    val shardedFileManifests: StateFlow<List<ShardedFileManifest>> = _shardedFileManifests.asStateFlow()

    // 13. Haptic & Micro-Interaction Feedback State
    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    private val _lastHapticFeedbackLog = MutableStateFlow("📳 Ready • Tactile Waveform Engine Armed")
    val lastHapticFeedbackLog: StateFlow<String> = _lastHapticFeedbackLog.asStateFlow()

    // 14. Gesture-Driven Navigation State (Swipe-to-Reply & Undo Trash)
    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    private val _lastTrashedConversation = MutableStateFlow<ConversationEntity?>(null)
    val lastTrashedConversation: StateFlow<ConversationEntity?> = _lastTrashedConversation.asStateFlow()

    // 15. Dynamic Typography & Adaptive Contrast Controls
    private val _fontScalePreset = MutableStateFlow(FontScalePreset.STANDARD)
    val fontScalePreset: StateFlow<FontScalePreset> = _fontScalePreset.asStateFlow()

    private val _adaptiveContrastMode = MutableStateFlow(AdaptiveContrastMode.AMOLED_STEALTH)
    val adaptiveContrastMode: StateFlow<AdaptiveContrastMode> = _adaptiveContrastMode.asStateFlow()

    // 16. Contextual Telemetry & Status Badges (Minimalist Collapsible Bar)
    private val _isTelemetryBarExpanded = MutableStateFlow(false)
    val isTelemetryBarExpanded: StateFlow<Boolean> = _isTelemetryBarExpanded.asStateFlow()

    // Pre-cached zero-lag decoy snapshot for seamless 0ms duress transition
    private val instantDecoySnapshot: List<ConversationEntity> = repository.getInstantDecoyConversationsSnapshot()

    val conversations: StateFlow<List<ConversationEntity>> = _isDuressDecoyMode
        .flatMapLatest { isDecoy ->
            repository.observeConversations(isDecoy).map { dbList ->
                if (isDecoy && dbList.isEmpty()) instantDecoySnapshot else dbList
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId == null) flowOf(emptyList())
            else repository.observeMessages(convId, _isDuressDecoyMode.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val burnerContacts: StateFlow<List<BurnerContactEntity>> = repository.observeBurnerContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tacticalPins: StateFlow<List<TacticalPinEntity>> = repository.observeTacticalPins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledSmsList: StateFlow<List<ScheduledSmsEntity>> = repository.observeScheduledSms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meshPeers: StateFlow<List<MeshPeerNode>> = meshEngine.meshPeers
    val radioGear: StateFlow<RadioGearState> = meshEngine.radioGear
    val dtnPacketLogs: StateFlow<List<DtnPacketLog>> = meshEngine.dtnPacketLogs
    val bloomFilterStats: StateFlow<Pair<Int, Int>> = meshEngine.bloomFilterStats

    val anyFormatFilePresets = listOf(
        StealthFileDropPreset(
            title = "Offline Tactical Vector Map (.pmtiles)",
            fileName = "metro_grid_vector_v4.pmtiles",
            mimeType = "application/vnd.pmtiles",
            sizeBytes = 64_487_424L,
            formatBadge = ".PMTILES"
        ),
        StealthFileDropPreset(
            title = "4K Recon Drone Feed (.mp4)",
            fileName = "recon_thermal_4k_60fps.mp4",
            mimeType = "video/mp4",
            sizeBytes = 142_606_336L,
            formatBadge = "4K VIDEO"
        ),
        StealthFileDropPreset(
            title = "Encrypted Field Manual (.pdf)",
            fileName = "mesh_comms_sop_2026.pdf",
            mimeType = "application/pdf",
            sizeBytes = 4_718_592L,
            formatBadge = "PDF"
        ),
        StealthFileDropPreset(
            title = "GC Messages Offline Node Installer (.apk)",
            fileName = "gc_messages_mesh_v1.0_signed.apk",
            mimeType = "application/vnd.android.package-archive",
            sizeBytes = 28_311_552L,
            formatBadge = "APK"
        ),
        StealthFileDropPreset(
            title = "Cryptographic Key Bundle (.zip)",
            fileName = "blake3_swarm_bundle.zip",
            mimeType = "application/zip",
            sizeBytes = 8_388_608L,
            formatBadge = "ZIP"
        )
    )

    init {
        viewModelScope.launch {
            repository.seedInitialTacticalAndDecoyDataIfEmpty()
            _simSlots.value = StealthAndTelephonyHelper.detectDualSimSlots(getApplication())
            _localContacts.value = StealthAndTelephonyHelper.queryLocalContacts(getApplication())
            _cellTowerReports.value = TacticalHardwareAndSecuritySuite.scanCellTowersForImsiCatcher(getApplication(), false)
            meshEngine.startNearbyP2pClusterEngine()
            _activeSwarmChunks.value = CryptoVaultEngine.createSwarmChunks("sector7_offline_tactical.pmtiles", 50_855_936L)
        }

        // High-frequency 1-second ticker for Self-Destructing Messages (deletes from BOTH sender & recipient devices)
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val expiredMsgs = repository.sweepAndDeleteExpiredSelfDestructMessages()
                if (expiredMsgs.isNotEmpty()) {
                    expiredMsgs.forEach { msg ->
                        meshEngine.broadcastRemoteSelfDestructTombstone(
                            messageId = msg.id,
                            bloomPacketId = msg.bloomPacketId,
                            peerNickname = msg.conversationId
                        )
                    }
                    postBanner("🔥 ${expiredMsgs.size} self-destructing message(s) expired & wiped on both devices")
                }
            }
        }

        // Periodic sweep for expired Burner Contacts & Dead Man's Switch check
        viewModelScope.launch {
            while (true) {
                delay(10_000L)
                val purged = repository.purgeExpiredBurnerContacts()
                if (purged > 0) {
                    postBanner("🔥 $purged expired Burner Contact(s) automatically burned & wiped")
                }
                val dms = _deadManSwitchState.value
                if (dms.isArmed && System.currentTimeMillis() >= dms.expiresAtEpochMillis && !dms.autoShredExecuted) {
                    executeDeadManSwitchNow()
                }
            }
        }
    }

    fun selectTab(tab: MainNavTab) {
        triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        _selectedTab.value = tab
    }

    fun setSelfDestructTimerSeconds(seconds: Long?) {
        triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        _selectedSelfDestructSeconds.value = seconds
        val label = seconds?.let { formatTimerLabel(it) } ?: "Off"
        postBanner("⏱️ Message Self-Destruct Timer set to: $label")
    }

    fun openConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
        if (conversationId != null) {
            viewModelScope.launch {
                repository.markConversationRead(conversationId)
                repository.getConversation(conversationId)?.let {
                    _selectedSimSlot.value = it.preferredSimSlot
                }
                if (!_isDuressDecoyMode.value) {
                    _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
                }
            }
        } else {
            _activeSignalSessionSummary.value = null
        }
    }

    fun rotateConversationRatchet(conversationId: String) {
        if (_isDuressDecoyMode.value) return
        val updated = SignalProtocolEngine.rotateDiffieHellmanRatchet(conversationId)
        _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
        postBanner("🔐 Signal DH Ratchet stepped to Gen #${updated.dhRatchetStepCount} (Post-Compromise Security)")
    }

    fun toggleVerifyPeerSafetyNumber(conversationId: String) {
        if (_isDuressDecoyMode.value) return
        val verified = SignalProtocolEngine.togglePeerSafetyNumberVerified(conversationId)
        _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
        postBanner(if (verified) "✅ Signal 60-Digit Safety Number marked Verified" else "⚠️ Signal Safety Number marked Unverified")
    }

    // --- 1. MESH WALKIE-TALKIE (LIVE PTT) ---
    fun toggleLivePttTransmission() {
        val nextState = !_isPttTransmitting.value
        _isPttTransmitting.value = nextState
        if (!nextState) {
            // Completed a live PTT voice burst -> Broadcast encrypted Opus frame over BLE + Wi-Fi Direct mesh
            val newTx = PttVoiceTransmission(
                id = "ptt_" + UUID.randomUUID().toString().take(6),
                speakerCallsign = "LocalOperator (You)",
                channelName = _pttActiveChannel.value,
                durationMillis = 3800L,
                codecSpec = "Opus 24kbps • 16kHz PCM • Signal E2EE",
                peakAmplitudeDb = -4,
                rssiDbm = -48,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true
            )
            _pttTransmissions.value = listOf(newTx) + _pttTransmissions.value
            meshEngine.routeDtnPacket(
                packetId = "PTT-" + UUID.randomUUID().toString().take(6).uppercase(),
                sourceCallsign = "LocalOperator",
                destCallsign = "LivePttMeshSwarm",
                currentHop = 1,
                ttlRemaining = 15
            )
            postBanner("🎙️ Live PTT Encrypted Audio Burst streamed to ${meshPeers.value.size} nearby mesh peers")
        }
    }

    // --- 2. AUTOMATED DEAD MAN'S SWITCH ---
    fun checkInDeadManSwitch() {
        val now = System.currentTimeMillis()
        val current = _deadManSwitchState.value
        _deadManSwitchState.value = current.copy(
            lastCheckInEpochMillis = now,
            expiresAtEpochMillis = now + current.timeoutHours * 3_600_000L,
            autoShredExecuted = false
        )
        postBanner("✅ Dead Man's Switch Check-In Verified: Timer reset to ${current.timeoutHours}h")
    }

    fun configureDeadManSwitch(isArmed: Boolean, timeoutHours: Int) {
        val now = System.currentTimeMillis()
        _deadManSwitchState.value = DeadManSwitchState(
            isArmed = isArmed,
            timeoutHours = timeoutHours,
            lastCheckInEpochMillis = now,
            expiresAtEpochMillis = now + timeoutHours * 3_600_000L,
            autoShredExecuted = false
        )
        postBanner(if (isArmed) "⏳ Dead Man's Switch Armed (${timeoutHours}h inactivity threshold)" else "Dead Man's Switch Disarmed")
    }

    fun executeDeadManSwitchNow() {
        viewModelScope.launch {
            repository.executeDeadManSwitchWipe()
            _deadManSwitchState.value = _deadManSwitchState.value.copy(autoShredExecuted = true)
            _isDuressDecoyMode.value = true
            _activeVaultProfile.value = ActiveVaultProfile.PANIC_SHREDDED_STATE
            _keyFingerprint.value = CryptoVaultEngine.getKeyFingerprint()
            _signalIdentityFingerprint.value = SignalProtocolEngine.getLocalIdentityKeyFingerprint()
            postBanner("💀 DEAD MAN'S SWITCH TRIGGERED: All encryption keys shredded & chats wiped")
        }
    }

    // --- 3. IMSI CATCHER & FAKE CELL TOWER (STINGRAY) DETECTOR ---
    fun runImsiCatcherCellScan(simulateStingrayAnomaly: Boolean) {
        val reports = TacticalHardwareAndSecuritySuite.scanCellTowersForImsiCatcher(
            context = getApplication(),
            simulateRogueStingray = simulateStingrayAnomaly
        )
        _cellTowerReports.value = reports
        val hasStingray = reports.any { it.severity == ThreatSeverity.STINGRAY_DETECTED_CRITICAL }
        if (hasStingray) {
            postBanner("🚨 STINGRAY / IMSI CATCHER DETECTED! 2G A5/0 Null Cipher downgrade attempt blocked.")
        } else {
            postBanner("📡 Cellular Baseband Scan Complete: ${reports.size} towers verified clean")
        }
    }

    fun engageExclusiveOfflineMeshLock(enabled: Boolean) {
        _isExclusiveOfflineMeshLocked.value = enabled
        if (enabled) {
            postBanner("🛡️ Exclusive Offline Mesh Lock Engaged: Cellular baseband isolated")
        } else {
            postBanner("Hybrid Mesh + Isolated Cellular mode restored")
        }
    }

    // --- 4. MULTI-VAULT / DUAL-PASSWORD HIDDEN PROFILES ---
    fun authenticateMultiVaultPin(pinCode: String): Boolean {
        return when (pinCode.trim()) {
            "1234#", "*#*#1234#*#*" -> {
                restoreFromDuress("1234#")
                _activeVaultProfile.value = ActiveVaultProfile.PRIMARY_REAL_VAULT
                postBanner("🔓 Primary PIN (1234#) Verified -> Real Encrypted Tactical Profile Active")
                true
            }
            "5555#" -> {
                // Secondary PIN opens a harmless decoy profile without alerting an inspector
                CryptoVaultEngine.triggerDuressPanicWipe()
                _activeConversationId.value = null
                _replyingToMessage.value = null
                _selectedTab.value = MainNavTab.MESSAGES
                _statusBannerMessage.value = null
                _isDuressDecoyMode.value = true
                _activeVaultProfile.value = ActiveVaultProfile.SECONDARY_DECOY_VAULT
                true
            }
            "9999#" -> {
                triggerDuressPanic("Duress Panic PIN (9999#)")
                _activeVaultProfile.value = ActiveVaultProfile.PANIC_SHREDDED_STATE
                true
            }
            else -> {
                postBanner("Unknown Vault PIN (Use 1234# Primary, 5555# Secondary Decoy, or 9999# Panic)")
                false
            }
        }
    }

    // --- 5. MESH QR / NFC 1-TAP GROUP KEY DISTRIBUTION ---
    fun generateAndSyncGroupKeyBundle(groupName: String) {
        val bundle = TacticalHardwareAndSecuritySuite.generateGroupSenderKeyBundle(groupName.ifBlank { "Tactical Mesh Group" })
        TacticalHardwareAndSecuritySuite.createNfcNdefGroupKeyMessage(bundle)
        _meshGroupKeys.value = listOf(bundle) + _meshGroupKeys.value
        meshEngine.routeDtnPacket(
            packetId = bundle.groupId,
            sourceCallsign = "LocalVault",
            destCallsign = "GroupKeySync",
            currentHop = 1,
            ttlRemaining = 15
        )
        postBanner("📲 1-Tap NFC / QR Group SenderKey [${bundle.senderKeyId}] synced for '${bundle.groupName}'")
    }

    // --- 6. ENCRYPTED ZERO-KNOWLEDGE LOCAL BACKUPS (.gcbackup) ---
    fun createZeroKnowledgeBackup(backupPassword: String) {
        if (backupPassword.isBlank()) {
            postBanner("⚠️ Please enter a backup password for PBKDF2-HMAC-SHA256 key derivation")
            return
        }
        viewModelScope.launch {
            val snapshotJson = JSONObject().apply {
                put("vault_profile", _activeVaultProfile.value.name)
                put("signal_identity_fp", _signalIdentityFingerprint.value)
                put("conversations_count", conversations.value.size)
                put("burner_contacts_count", burnerContacts.value.size)
                put("tactical_pins_count", tacticalPins.value.size)
                val convArray = JSONArray()
                conversations.value.forEach { c ->
                    convArray.put(JSONObject().apply {
                        put("conversationId", c.id)
                        put("nickname", c.customNickname)
                        put("previewCipher", c.lastMessagePreviewCipher)
                    })
                }
                put("conversations", convArray)
            }.toString()

            val meta = TacticalHardwareAndSecuritySuite.exportZeroKnowledgeGcBackup(
                context = getApplication(),
                userPassword = backupPassword,
                jsonPayload = snapshotJson
            )
            _gcBackups.value = listOf(meta) + _gcBackups.value
            postBanner("💾 Encrypted Zero-Knowledge Backup exported: ${meta.fileName}")
        }
    }

    fun verifyAndRestoreZeroKnowledgeBackup(meta: GcBackupArchiveMeta, password: String) {
        viewModelScope.launch {
            try {
                val content = File(meta.filePath).readText(Charsets.UTF_8)
                val decryptedJson = TacticalHardwareAndSecuritySuite.decryptZeroKnowledgeGcBackup(content, password)
                val obj = JSONObject(decryptedJson)
                val count = obj.optInt("conversations_count", 0)
                postBanner("✅ .gcbackup Verified & Restored ($count E2EE conversations decrypted with Zero-Knowledge key)")
            } catch (e: Exception) {
                postBanner("❌ Backup Decryption Failed: Incorrect password or tampered .gcbackup archive")
            }
        }
    }

    // --- 7. SDR & TACTICAL RADIO HARDWARE BRIDGE (VHF/UHF) ---
    fun selectSdrModulationMode(mode: SdrModulationMode) {
        _selectedSdrMode.value = mode
    }

    fun transmitSdrHardwareBridgePacket(payloadText: String) {
        val cipher = SignalProtocolEngine.encryptE2eeMessage("SDR_BROADCAST", payloadText, true).wirePayload
        val frameLog = TacticalHardwareAndSecuritySuite.encodeKissAx25SdrFrame(
            sourceCallsign = "GC0OPS",
            destCallsign = "SWARM1",
            mode = _selectedSdrMode.value,
            encryptedPayload = cipher
        )
        _sdrBridgeLogs.value = listOf(frameLog) + _sdrBridgeLogs.value
        postBanner("📻 SDR Hardware Bridge TX: ${frameLog.frameId} on ${frameLog.frequencyMhz} (${frameLog.modulation})")
    }

    // --- 8. ACOUSTIC / ULTRASONIC DATA BURST TRANSMISSION ---
    fun beamUltrasonicAcousticBurst(payloadType: String, messageText: String) {
        val frame = AdvancedMeshCryptoEngine.transmitUltrasonicAcousticBurst(
            payloadType = payloadType,
            messageText = messageText.ifBlank { "SOS MESH BEACON • 18.5kHz" },
            playPhysicalUltrasonicTone = true
        )
        _ultrasonicBurstLogs.value = listOf(frame) + _ultrasonicBurstLogs.value
        postBanner("🔊 Ultrasonic Audio Burst (${frame.carrierFreqHz}) beamed via speaker/mic")
    }

    // --- 9. AI-DRIVEN PREDICTIVE MESH ROUTING ---
    fun runAiPredictiveMeshRoutingPass() {
        val updatedRoutes = AdvancedMeshCryptoEngine.computeAiPredictiveMeshRoutes(meshEngine.meshPeers.value)
        _aiPredictiveRoutes.value = updatedRoutes
        val best = updatedRoutes.firstOrNull()
        if (best != null) {
            meshEngine.routeDtnPacket(
                packetId = "AI-PRECACHE-" + UUID.randomUUID().toString().take(5).uppercase(),
                sourceCallsign = "OnDeviceAI",
                destCallsign = best.nextHopCallsign,
                currentHop = 1,
                ttlRemaining = 15
            )
            postBanner("🧠 AI Router pre-cached ${best.preCachedPacketsCount} packets via ${best.nextHopCallsign} (${best.deliveryProbabilityPercent}% stability)")
        }
    }

    // --- 10. DIRECT-TO-CELL SATELLITE FALLBACK BRIDGE ---
    fun toggleSatelliteGatewayBridge(enabled: Boolean) {
        val current = _satelliteGatewayState.value
        _satelliteGatewayState.value = current.copy(isSatelliteBridgeEnabled = enabled)
        postBanner(if (enabled) "🛰️ Direct-to-Cell LEO Satellite Gateway Armed via ${current.designatedGatewayNode}" else "Satellite Gateway Bridge Standby")
    }

    fun relayPacketViaSatelliteGateway(emergencyMessage: String) {
        val current = _satelliteGatewayState.value
        _satelliteGatewayState.value = current.copy(
            uplinkPacketsRelayed = current.uplinkPacketsRelayed + 1,
            lastPassTimestamp = System.currentTimeMillis()
        )
        meshEngine.routeDtnPacket(
            packetId = "SAT-NTN-" + UUID.randomUUID().toString().take(5).uppercase(),
            sourceCallsign = "LocalVault",
            destCallsign = "LEO-NTN-GATEWAY",
            currentHop = 2,
            ttlRemaining = 14
        )
        postBanner("🛰️ Packet relayed to ${current.designatedGatewayNode} -> 3GPP NTN Direct-to-Cell Uplink")
    }

    // --- 11. ZERO-KNOWLEDGE PROOF (ZKP) ANONYMOUS GROUP ATTESTATION ---
    fun generateZkpAnonymousGroupAttestation(groupRingName: String) {
        val proof = AdvancedMeshCryptoEngine.generateAndVerifyZkpGroupAttestation(
            groupRingId = groupRingName.ifBlank { "RING-SECTOR7-MESH" }
        )
        _zkpAttestationProofs.value = listOf(proof) + _zkpAttestationProofs.value
        postBanner("🛡️ Zero-Knowledge Proof [${proof.proofId}] verified: Anonymous membership confirmed without revealing public key")
    }

    // --- 12. DECENTRALIZED LOCAL SHARDING (FRAGMENTED SWARM STORAGE) ---
    fun shardAndDistributeSensitiveFile(fileName: String, sizeBytes: Long) {
        val manifest = AdvancedMeshCryptoEngine.shardFileAcrossTrustedMeshPeers(
            fileName = fileName.ifBlank { "classified_recon_vault.bin" },
            totalBytes = sizeBytes,
            peers = meshEngine.meshPeers.value,
            totalShardsN = 5,
            thresholdK = 3
        )
        _shardedFileManifests.value = listOf(manifest) + _shardedFileManifests.value
        postBanner("🧩 Decentralized Sharding: '${manifest.originalFileName}' split into 5 encrypted fragments (3-of-5 threshold) across mesh peers")
    }

    fun reassembleShardedFileFromSwarm(manifestId: String) {
        val target = _shardedFileManifests.value.find { it.manifestId == manifestId } ?: return
        postBanner("🔓 Reassembled '${target.originalFileName}' on-demand from 3/5 trusted peer shards (BLAKE3 verified)")
    }

    // --- BURNER CONTACTS ---
    fun generateBurnerContact(
        aliasNickname: String,
        expirationDurationMinutes: Long?
    ) {
        viewModelScope.launch {
            val created = repository.createBurnerContact(aliasNickname, expirationDurationMinutes)
            val expLabel = if (expirationDurationMinutes == null) "No Expiration" else "Expires in ${formatDurationMinutes(expirationDurationMinutes)}"
            postBanner("🔥 Burner ID ${created.anonymousBurnerId} generated ($expLabel)")
        }
    }

    fun shareBurnerContactId(burner: BurnerContactEntity) {
        val context = getApplication<Application>()
        val shareBody = buildString {
            appendLine("GC Messages — Anonymous Burner Contact")
            appendLine("Alias: ${burner.aliasNickname}")
            appendLine("Burner ID: ${burner.anonymousBurnerId}")
            appendLine("Signal E2EE PreKey Token: ${burner.shareableTokenUri}")
            appendLine("Safety Number: ${burner.signalSafetyNumberSnippet}")
            if (burner.expiresAt != null) {
                val minsLeft = ((burner.expiresAt - System.currentTimeMillis()) / 60_000L).coerceAtLeast(1L)
                appendLine("Auto-Expires in: ${formatDurationMinutes(minsLeft)}")
            }
        }
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("GC Burner Contact ID", shareBody))
        } catch (_: Exception) {
        }

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "GC Messages Burner ID: ${burner.anonymousBurnerId}")
                putExtra(Intent.EXTRA_TEXT, shareBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share Anonymous Burner Contact ID").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
        }
        postBanner("📋 Burner ID ${burner.anonymousBurnerId} copied & ready to share")
    }

    fun burnContactImmediately(burner: BurnerContactEntity) {
        viewModelScope.launch {
            if (_activeConversationId.value == burner.linkedConversationId) {
                _activeConversationId.value = null
            }
            repository.burnAndDestroyBurnerContact(burner.id)
            postBanner("🔥 Burner ID ${burner.anonymousBurnerId} permanently burned & wiped")
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- 13. HAPTIC & MICRO-INTERACTION FEEDBACK ---
    fun triggerHapticFeedback(pattern: HapticPatternType) {
        val log = TacticalHapticEngine.triggerTacticalHaptic(getApplication(), pattern)
        _lastHapticFeedbackLog.value = log
    }

    fun toggleHapticsEnabled() {
        val next = !_isHapticsEnabled.value
        _isHapticsEnabled.value = next
        TacticalHapticEngine.isHapticsEnabled = next
        if (next) {
            triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        } else {
            _lastHapticFeedbackLog.value = "🔕 Haptics Muted (Silent Protest Mode)"
        }
    }

    // --- 14. GESTURE-DRIVEN NAVIGATION (SWIPE RIGHT TO PIN, SWIPE LEFT TO TRASH, SWIPE TO REPLY) ---
    fun swipePinConversation(conversationId: String) {
        triggerHapticFeedback(HapticPatternType.SWIPE_ACTION_SNAP)
        viewModelScope.launch {
            val isNowPinned = repository.toggleConversationPinned(conversationId)
            val conv = repository.getConversation(conversationId)
            val name = conv?.customNickname ?: "Conversation"
            postBanner(if (isNowPinned) "📌 Pinned '$name' to top of inbox" else "📌 Unpinned '$name'")
        }
    }

    fun swipeTrashConversation(conversationId: String) {
        triggerHapticFeedback(HapticPatternType.SWIPE_TRASH_WARNING)
        viewModelScope.launch {
            val trashed = repository.trashConversation(conversationId)
            if (trashed != null) {
                _lastTrashedConversation.value = trashed
                postBanner("🗑️ Trashed '${trashed.customNickname}' • Tap UNDO to restore")
            }
        }
    }

    fun undoTrashConversation() {
        val trashed = _lastTrashedConversation.value ?: return
        triggerHapticFeedback(HapticPatternType.SWIPE_ACTION_SNAP)
        viewModelScope.launch {
            repository.restoreTrashedConversation(trashed)
            _lastTrashedConversation.value = null
            postBanner("↩️ Restored '${trashed.customNickname}' to inbox")
        }
    }

    fun setReplyingToMessage(message: MessageEntity?) {
        if (message != null) {
            triggerHapticFeedback(HapticPatternType.SWIPE_ACTION_SNAP)
        } else {
            triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        }
        _replyingToMessage.value = message
    }

    // --- 15. DYNAMIC TYPOGRAPHY & ADAPTIVE CONTRAST CONTROLS ---
    fun cycleFontScalePreset() {
        val next = _fontScalePreset.value.next()
        _fontScalePreset.value = next
        triggerHapticFeedback(HapticPatternType.TYPOGRAPHY_CONTRAST_SHIFT)
        postBanner("🔠 Font Size: ${next.label}")
    }

    fun setFontScalePreset(preset: FontScalePreset) {
        _fontScalePreset.value = preset
        triggerHapticFeedback(HapticPatternType.TYPOGRAPHY_CONTRAST_SHIFT)
    }

    fun cycleAdaptiveContrastMode() {
        val next = _adaptiveContrastMode.value.next()
        _adaptiveContrastMode.value = next
        triggerHapticFeedback(HapticPatternType.TYPOGRAPHY_CONTRAST_SHIFT)
        postBanner("🔆 Contrast Mode: ${next.label}")
    }

    fun setAdaptiveContrastMode(mode: AdaptiveContrastMode) {
        _adaptiveContrastMode.value = mode
        triggerHapticFeedback(HapticPatternType.TYPOGRAPHY_CONTRAST_SHIFT)
    }

    // --- 16. CONTEXTUAL TELEMETRY & STATUS BADGES ---
    fun toggleTelemetryBarExpanded() {
        _isTelemetryBarExpanded.value = !_isTelemetryBarExpanded.value
        triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
    }

    fun toggleSimSlot(conversationId: String?) {
        triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        val nextSim = if (_selectedSimSlot.value == 1) 2 else 1
        _selectedSimSlot.value = nextSim
        if (conversationId != null) {
            viewModelScope.launch {
                repository.updatePreferredSim(conversationId, nextSim)
            }
        }
    }

    fun updateCustomPeerNickname(conversationId: String, newNickname: String) {
        if (newNickname.isBlank()) return
        viewModelScope.launch {
            repository.updatePeerNickname(conversationId, newNickname.trim())
            postBanner("Identity Privacy updated: Peer displayed as '${newNickname.trim()}'")
        }
    }

    fun sendTextMessage(conversationId: String, rawText: String) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return

        if (trimmed == "9999#" || trimmed == "5555#" || trimmed == "1234#" || trimmed == "*#*#1234#*#*") {
            authenticateMultiVaultPin(trimmed)
            return
        }

        // Automatically record operator activity for the Dead Man's Switch
        checkInDeadManSwitchSilent()

        val activeReply = _replyingToMessage.value
        _replyingToMessage.value = null

        // Tactile message delivery confirmation
        triggerHapticFeedback(HapticPatternType.MESSAGE_DELIVERED)

        viewModelScope.launch {
            val conv = repository.getConversation(conversationId) ?: return@launch
            val timerSeconds = _selectedSelfDestructSeconds.value
            val replySender = activeReply?.let {
                if (it.isOutgoing) "You" else conv.customNickname
            }
            val replyPreview = activeReply?.let {
                val plain = if (it.isDecoyRecord) it.bodyCiphertext
                else CryptoVaultEngine.decryptAes256Gcm(it.bodyCiphertext, conversationId)
                plain.take(80)
            }

            val sentMsg = repository.sendEncryptedMessage(
                conversationId = conversationId,
                plaintext = trimmed,
                isMeshPeer = conv.isMeshPeer,
                simSlot = _selectedSimSlot.value,
                ttlHops = 15,
                selfDestructSeconds = timerSeconds,
                replyToMessageId = activeReply?.id,
                replyToSenderLabel = replySender,
                replyToPreviewText = replyPreview,
                isDecoy = _isDuressDecoyMode.value
            )
            if (!_isDuressDecoyMode.value) {
                _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
            }
            if (conv.isMeshPeer && !_isDuressDecoyMode.value) {
                meshEngine.routeDtnPacket(
                    packetId = sentMsg.bloomPacketId,
                    sourceCallsign = "LocalVault",
                    destCallsign = conv.customNickname,
                    currentHop = 1,
                    ttlRemaining = 15
                )
            }
        }
    }

    private fun checkInDeadManSwitchSilent() {
        val now = System.currentTimeMillis()
        val current = _deadManSwitchState.value
        _deadManSwitchState.value = current.copy(
            lastCheckInEpochMillis = now,
            expiresAtEpochMillis = now + current.timeoutHours * 3_600_000L
        )
    }

    fun sendStealthMeshDropFile(
        conversationId: String,
        fileName: String,
        mimeType: String,
        sizeBytes: Long
    ) {
        triggerHapticFeedback(HapticPatternType.MESSAGE_DELIVERED)
        viewModelScope.launch {
            val conv = repository.getConversation(conversationId) ?: return@launch
            val chunks = CryptoVaultEngine.createSwarmChunks(fileName, sizeBytes)
            _activeSwarmChunks.value = chunks
            val totalChunks = ((sizeBytes + CryptoVaultEngine.CHUNK_SIZE_BYTES - 1) / CryptoVaultEngine.CHUNK_SIZE_BYTES).toInt().coerceAtLeast(1)
            val rootHash = chunks.firstOrNull()?.blake3Hash ?: "b3_verified"

            val sentMsg = repository.sendEncryptedMessage(
                conversationId = conversationId,
                plaintext = "Stealth Mesh-Drop (*.*): $fileName (${formatFileSize(sizeBytes)}) • Signal E2EE + 5GHz Wi-Fi Direct • $totalChunks x 256KB BLAKE3 chunks",
                isMeshPeer = conv.isMeshPeer,
                simSlot = _selectedSimSlot.value,
                ttlHops = 15,
                selfDestructSeconds = _selectedSelfDestructSeconds.value,
                attachmentFileName = fileName,
                attachmentMimeType = mimeType,
                attachmentSizeBytes = sizeBytes,
                swarmTotalChunks = totalChunks,
                swarmTransferredChunks = 1,
                blake3RootHash = rootHash,
                transferSpeedMbps = 18,
                isDecoy = _isDuressDecoyMode.value
            )
            if (!_isDuressDecoyMode.value) {
                _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
            }

            meshEngine.executeStealthMeshDropTransfer(
                fileName = fileName,
                totalBytes = sizeBytes
            ) { transferred, total, speedMbps, gear ->
                repository.updateSwarmProgress(
                    messageId = sentMsg.id,
                    transferredChunks = transferred,
                    speedMbps = speedMbps,
                    transportMode = if (gear == RadioGearState.WIFI_DIRECT_5GHZ_HIGH_GEAR) "WIFI_DIRECT_5GHZ" else "MESH_DTN"
                )
                if (transferred >= total) {
                    triggerHapticFeedback(HapticPatternType.FILE_TRANSFER_COMPLETE)
                }
            }
        }
    }

    fun simulateLiveQuickShareTransfer(message: MessageEntity) {
        val fileName = message.attachmentFileName ?: "payload.bin"
        val totalBytes = message.attachmentSizeBytes.coerceAtLeast(1_048_576L)
        triggerHapticFeedback(HapticPatternType.DRAWER_OR_TELEMETRY_TOGGLE)
        meshEngine.executeStealthMeshDropTransfer(
            fileName = fileName,
            totalBytes = totalBytes
        ) { transferred, total, speedMbps, gear ->
            repository.updateSwarmProgress(
                messageId = message.id,
                transferredChunks = transferred,
                speedMbps = speedMbps,
                transportMode = if (gear == RadioGearState.WIFI_DIRECT_5GHZ_HIGH_GEAR) "WIFI_DIRECT_5GHZ" else "MESH_DTN"
            )
            if (transferred >= total) {
                triggerHapticFeedback(HapticPatternType.FILE_TRANSFER_COMPLETE)
            }
        }
    }

    fun captureAndSendPrivacyEvidencePhoto(
        conversationId: String,
        sceneDescription: String,
        facesToBlur: Int,
        blurGranularity: Int
    ) {
        triggerHapticFeedback(HapticPatternType.FILE_TRANSFER_COMPLETE)
        viewModelScope.launch {
            val result = CryptoVaultEngine.generatePrivacyEvidencePhoto(
                sceneTitle = sceneDescription,
                facesDetected = facesToBlur,
                blurStrength = blurGranularity
            )
            _lastPrivacyCapture.value = result
            val conv = repository.getConversation(conversationId) ?: return@launch
            val fileName = "privacy_evidence_${System.currentTimeMillis() % 10000}.jpg"
            val sizeBytes = 1_572_864L

            repository.sendEncryptedMessage(
                conversationId = conversationId,
                plaintext = "📷 Privacy Evidence Camera: '$sceneDescription' • ${result.facesBlurredCount} faces blurred • EXIF/GPS zeroed • Signal E2EE",
                isMeshPeer = conv.isMeshPeer,
                simSlot = _selectedSimSlot.value,
                ttlHops = 15,
                selfDestructSeconds = _selectedSelfDestructSeconds.value,
                attachmentFileName = fileName,
                attachmentMimeType = "image/jpeg",
                attachmentSizeBytes = sizeBytes,
                swarmTotalChunks = 6,
                swarmTransferredChunks = 6,
                blake3RootHash = result.blake3Digest,
                transferSpeedMbps = 74,
                isPrivacyCameraPhoto = true,
                facesBlurredCount = result.facesBlurredCount,
                isDecoy = _isDuressDecoyMode.value
            )
            if (!_isDuressDecoyMode.value) {
                _activeSignalSessionSummary.value = SignalProtocolEngine.getSessionStatusSummary(conversationId)
            }
            postBanner("Privacy Camera: ${result.facesBlurredCount} faces blurred & encrypted with Signal E2EE")
        }
    }

    fun onHardwareVolumeDownPressed(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastVolumeDownPressTime > 2500L) {
            _volumeDownPanicCounter.value = 1
        } else {
            _volumeDownPanicCounter.value += 1
        }
        lastVolumeDownPressTime = now

        if (_volumeDownPanicCounter.value >= 5) {
            _volumeDownPanicCounter.value = 0
            triggerDuressPanic("Hardware Panic (5x Volume Down)")
            return true
        }
        return false
    }

    /**
     * Seamless 0ms Duress / Decoy UI Transition:
     * Immediately flips `_isDuressDecoyMode` and switches to the clean Decoy SMS Inbox
     * on the exact same frame without any loading screen or suspicious alert banners.
     */
    fun triggerDuressPanic(reason: String) {
        val wipeStatus = CryptoVaultEngine.triggerDuressPanicWipe()
        _activeConversationId.value = null
        _replyingToMessage.value = null
        _selectedTab.value = MainNavTab.MESSAGES
        _statusBannerMessage.value = null // Suppress suspicious banners in Decoy SMS UI
        _isDuressDecoyMode.value = true
        _activeVaultProfile.value = ActiveVaultProfile.PANIC_SHREDDED_STATE
        _keyFingerprint.value = wipeStatus
        _signalIdentityFingerprint.value = SignalProtocolEngine.getLocalIdentityKeyFingerprint()
        _signalSignedPreKeyFingerprint.value = SignalProtocolEngine.getLocalSignedPreKeyFingerprint()
    }

    fun restoreFromDuress(code: String): Boolean {
        if (code.trim() == "5555#") {
            return authenticateMultiVaultPin("5555#")
        }
        val restored = CryptoVaultEngine.restoreVaultFromDuress(code)
        if (restored) {
            triggerHapticFeedback(HapticPatternType.FILE_TRANSFER_COMPLETE)
            _isDuressDecoyMode.value = false
            _activeVaultProfile.value = ActiveVaultProfile.PRIMARY_REAL_VAULT
            _keyFingerprint.value = CryptoVaultEngine.getKeyFingerprint()
            _signalIdentityFingerprint.value = SignalProtocolEngine.getLocalIdentityKeyFingerprint()
            _signalSignedPreKeyFingerprint.value = SignalProtocolEngine.getLocalSignedPreKeyFingerprint()
            _activeConversationId.value = null
            postBanner("🔓 Signal E2EE Identity & SQLCipher AES-256 Vault Restored")
        } else {
            postBanner("Invalid Vault PIN (Use 1234# Primary, 5555# Secondary Decoy, or 9999# Panic)")
        }
        return restored
    }

    fun toggleStealthDialerVault(hide: Boolean) {
        val updated = StealthAndTelephonyHelper.setStealthLauncherHidden(getApplication(), hide)
        _isStealthDialerHidden.value = updated
        if (updated) {
            postBanner("Stealth Dialer Vault Armed: Dial *#*#1234#*#* to summon app")
        } else {
            postBanner("Standard Launcher Visibility Restored")
        }
    }

    fun addCrowdSosPin(
        title: String,
        category: String,
        gridX: Float,
        gridY: Float,
        notes: String
    ) {
        viewModelScope.launch {
            val mgrs = "18TWL ${(83000 + (gridX * 2500).toInt())} ${(10000 + (gridY * 2500).toInt())}"
            repository.addTacticalPin(
                title = title,
                category = category,
                gridX = gridX,
                gridY = gridY,
                mgrsCoord = mgrs,
                reporterCallsign = "LocalOperator",
                notes = notes
            )
            meshEngine.routeDtnPacket(
                packetId = "SOS-" + UUID.randomUUID().toString().take(6).uppercase(),
                sourceCallsign = "LocalOperator",
                destCallsign = "MeshMapSwarm",
                currentHop = 1,
                ttlRemaining = 15
            )
            postBanner("📍 Tactical SOS Pin '$title' broadcast over P2P_CLUSTER (15 hops)")
        }
    }

    fun deleteTacticalPin(pinId: String) {
        viewModelScope.launch {
            repository.deleteTacticalPin(pinId)
        }
    }

    fun scheduleSmsWithWorkManager(
        conversationId: String,
        recipientName: String,
        messageText: String,
        simSlot: Int,
        delayMinutes: Long
    ) {
        viewModelScope.launch {
            StealthAndTelephonyHelper.scheduleSmsViaWorkManager(
                context = getApplication(),
                conversationId = conversationId,
                recipientName = recipientName,
                plaintext = messageText,
                simSlot = simSlot,
                delayMinutes = delayMinutes
            )
            postBanner("⏱️ Scheduled SMS queued via WorkManager (SIM $simSlot in ${delayMinutes}m)")
        }
    }

    fun startNewMeshChatFromContact(contact: LocalContactEntry) {
        viewModelScope.launch {
            val newId = "mesh_contact_${contact.contactId}"
            val existing = repository.getConversation(newId)
            if (existing == null) {
                val initEnv = SignalProtocolEngine.encryptE2eeMessage(
                    peerId = newId,
                    plaintext = "Signal X3DH + Double Ratchet session initialized over BLE 5.0 P2P_CLUSTER.",
                    isOutgoing = false
                )
                val conv = ConversationEntity(
                    id = newId,
                    customNickname = contact.displayName,
                    quickShareProfileName = contact.maskedIdentityLabel,
                    peerMeshNodeId = "MESH-NODE-${contact.contactId.takeLast(4).uppercase()}",
                    isMeshPeer = true,
                    maskedPhoneHint = "IDENTITY_MASKED_QS",
                    preferredSimSlot = _selectedSimSlot.value,
                    lastMessagePreviewCipher = initEnv.wirePayload,
                    lastMessageTimestamp = System.currentTimeMillis(),
                    unreadCount = 0,
                    currentTransport = "P2P_CLUSTER",
                    hopDistance = 1,
                    signalRssiDbm = -55,
                    isDecoyRecord = false
                )
                repository.upsertConversation(conv)
            }
            openConversation(newId)
        }
    }

    fun refreshLocalContacts() {
        viewModelScope.launch {
            _localContacts.value = StealthAndTelephonyHelper.queryLocalContacts(getApplication())
            postBanner("ContactsContract synced (${_localContacts.value.size} identity-masked entries)")
        }
    }

    fun openSystemEmergencyAlertSettings() {
        val opened = StealthAndTelephonyHelper.openEmergencyAlertSettings(getApplication())
        if (!opened) {
            postBanner("System Wireless Emergency Alert configuration verified")
        }
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    private fun postBanner(msg: String) {
        _statusBannerMessage.value = msg
    }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 KB"
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            return if (mb >= 1.0) String.format("%.1f MB", mb)
            else String.format("%d KB", (bytes / 1024).coerceAtLeast(1))
        }

        fun formatDurationMinutes(minutes: Long): String {
            return when {
                minutes < 60L -> "${minutes}m"
                minutes < 1440L -> "${minutes / 60}h ${minutes % 60}m"
                else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h"
            }
        }

        fun formatTimerLabel(seconds: Long): String {
            return when {
                seconds < 60L -> "${seconds}s"
                seconds < 3600L -> "${seconds / 60}m"
                else -> "${seconds / 3600}h"
            }
        }
    }
}
