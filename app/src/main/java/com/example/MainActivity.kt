package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BurnerContactsScreen
import com.example.ui.screens.ConversationInboxScreen
import com.example.ui.screens.MeshRadarAndDropScreen
import com.example.ui.screens.OfflineTacticalMapScreen
import com.example.ui.screens.SamsungChatThreadScreen
import com.example.ui.screens.StealthSecurityVaultScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElevatedCardBg
import com.example.ui.theme.MeshCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OutgoingSamsungTeal
import com.example.ui.theme.PrimaryTextWhite
import com.example.ui.theme.TimestampMutedGrey
import com.example.viewmodel.GcMessagesViewModel
import com.example.viewmodel.MainNavTab

class MainActivity : ComponentActivity() {

    private val viewModel: GcMessagesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("OPENED_FROM_SECRET_DIALER", false) == true) {
            viewModel.restoreFromDuress("*#*#1234#*#*")
        }

        setContent {
            MyApplicationTheme {
                GcMessagesRootApp(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val triggered = viewModel.onHardwareVolumeDownPressed()
            if (triggered) return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun GcMessagesRootApp(viewModel: GcMessagesViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val burnerContacts by viewModel.burnerContacts.collectAsStateWithLifecycle()
    val isDuressDecoyMode by viewModel.isDuressDecoyMode.collectAsStateWithLifecycle()
    val activeVaultProfile by viewModel.activeVaultProfile.collectAsStateWithLifecycle()
    val selectedSelfDestructSeconds by viewModel.selectedSelfDestructSeconds.collectAsStateWithLifecycle()
    val keyFingerprint by viewModel.keyFingerprint.collectAsStateWithLifecycle()
    val signalIdentityFingerprint by viewModel.signalIdentityFingerprint.collectAsStateWithLifecycle()
    val signalSignedPreKeyFingerprint by viewModel.signalSignedPreKeyFingerprint.collectAsStateWithLifecycle()
    val activeSignalSessionSummary by viewModel.activeSignalSessionSummary.collectAsStateWithLifecycle()
    val volumeDownPanicCount by viewModel.volumeDownPanicCounter.collectAsStateWithLifecycle()
    val isStealthDialerHidden by viewModel.isStealthDialerHidden.collectAsStateWithLifecycle()
    val deadManSwitchState by viewModel.deadManSwitchState.collectAsStateWithLifecycle()
    val cellTowerReports by viewModel.cellTowerReports.collectAsStateWithLifecycle()
    val isExclusiveOfflineMeshLocked by viewModel.isExclusiveOfflineMeshLocked.collectAsStateWithLifecycle()
    val gcBackups by viewModel.gcBackups.collectAsStateWithLifecycle()
    val isPttTransmitting by viewModel.isPttTransmitting.collectAsStateWithLifecycle()
    val pttActiveChannel by viewModel.pttActiveChannel.collectAsStateWithLifecycle()
    val pttTransmissions by viewModel.pttTransmissions.collectAsStateWithLifecycle()
    val ultrasonicBursts by viewModel.ultrasonicBurstLogs.collectAsStateWithLifecycle()
    val aiPredictiveRoutes by viewModel.aiPredictiveRoutes.collectAsStateWithLifecycle()
    val satelliteGatewayState by viewModel.satelliteGatewayState.collectAsStateWithLifecycle()
    val meshGroupKeys by viewModel.meshGroupKeys.collectAsStateWithLifecycle()
    val zkpProofs by viewModel.zkpAttestationProofs.collectAsStateWithLifecycle()
    val shardedManifests by viewModel.shardedFileManifests.collectAsStateWithLifecycle()
    val selectedSdrMode by viewModel.selectedSdrMode.collectAsStateWithLifecycle()
    val sdrBridgeLogs by viewModel.sdrBridgeLogs.collectAsStateWithLifecycle()
    val radioGear by viewModel.radioGear.collectAsStateWithLifecycle()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()
    val simSlots by viewModel.simSlots.collectAsStateWithLifecycle()
    val localContacts by viewModel.localContacts.collectAsStateWithLifecycle()
    val meshPeers by viewModel.meshPeers.collectAsStateWithLifecycle()
    val dtnPacketLogs by viewModel.dtnPacketLogs.collectAsStateWithLifecycle()
    val bloomFilterStats by viewModel.bloomFilterStats.collectAsStateWithLifecycle()
    val swarmChunks by viewModel.activeSwarmChunks.collectAsStateWithLifecycle()
    val tacticalPins by viewModel.tacticalPins.collectAsStateWithLifecycle()
    val scheduledSmsList by viewModel.scheduledSmsList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
    val lastHapticFeedbackLog by viewModel.lastHapticFeedbackLog.collectAsStateWithLifecycle()
    val replyingToMessage by viewModel.replyingToMessage.collectAsStateWithLifecycle()
    val lastTrashedConversation by viewModel.lastTrashedConversation.collectAsStateWithLifecycle()
    val fontScalePreset by viewModel.fontScalePreset.collectAsStateWithLifecycle()
    val adaptiveContrastMode by viewModel.adaptiveContrastMode.collectAsStateWithLifecycle()
    val isTelemetryBarExpanded by viewModel.isTelemetryBarExpanded.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.meshEngine.startNearbyP2pClusterEngine()
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            permissionLauncher.launch(perms.toTypedArray())
        } catch (_: Exception) {
        }
    }

    val activeConversation = conversations.find { it.id == activeConversationId }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        bottomBar = {
            if (activeConversation == null) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = PrimaryTextWhite
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.MESSAGES,
                        onClick = { viewModel.selectTab(MainNavTab.MESSAGES) },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Messages") },
                        label = { Text("Messages", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = OutgoingSamsungTeal,
                            indicatorColor = OutgoingSamsungTeal,
                            unselectedIconColor = TimestampMutedGrey,
                            unselectedTextColor = TimestampMutedGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_messages")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.BURNER_IDS,
                        onClick = { viewModel.selectTab(MainNavTab.BURNER_IDS) },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Burner Contacts") },
                        label = { Text("Burner IDs", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = OutgoingSamsungTeal,
                            indicatorColor = OutgoingSamsungTeal,
                            unselectedIconColor = TimestampMutedGrey,
                            unselectedTextColor = TimestampMutedGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_burners")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.MESH_RADAR,
                        onClick = { viewModel.selectTab(MainNavTab.MESH_RADAR) },
                        icon = { Icon(Icons.Default.Radar, contentDescription = "Mesh & Drop") },
                        label = { Text("Mesh", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = OutgoingSamsungTeal,
                            indicatorColor = OutgoingSamsungTeal,
                            unselectedIconColor = TimestampMutedGrey,
                            unselectedTextColor = TimestampMutedGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_mesh")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.TACTICAL_MAP,
                        onClick = { viewModel.selectTab(MainNavTab.TACTICAL_MAP) },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Tactical Map") },
                        label = { Text("Map", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = OutgoingSamsungTeal,
                            indicatorColor = OutgoingSamsungTeal,
                            unselectedIconColor = TimestampMutedGrey,
                            unselectedTextColor = TimestampMutedGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_map")
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainNavTab.STEALTH_VAULT,
                        onClick = { viewModel.selectTab(MainNavTab.STEALTH_VAULT) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Stealth Vault") },
                        label = { Text("Vault", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = OutgoingSamsungTeal,
                            indicatorColor = OutgoingSamsungTeal,
                            unselectedIconColor = TimestampMutedGrey,
                            unselectedTextColor = TimestampMutedGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_vault")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = statusBanner != null && !isDuressDecoyMode) {
                statusBanner?.let { bannerText ->
                    Surface(
                        color = ElevatedCardBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.dismissBanner() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bannerText,
                                color = MeshCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "DISMISS",
                                color = TimestampMutedGrey,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (activeConversation != null) {
                    SamsungChatThreadScreen(
                        conversation = activeConversation,
                        messages = activeMessages,
                        radioGear = radioGear,
                        peerCount = meshPeers.size,
                        isExclusiveOfflineLocked = isExclusiveOfflineMeshLocked,
                        selectedSimSlot = selectedSimSlot,
                        selectedSelfDestructSeconds = selectedSelfDestructSeconds,
                        selfDestructPresets = viewModel.selfDestructPresets,
                        signalSessionSummary = activeSignalSessionSummary,
                        anyFormatPresets = viewModel.anyFormatFilePresets,
                        replyingToMessage = replyingToMessage,
                        fontScalePreset = fontScalePreset,
                        adaptiveContrastMode = adaptiveContrastMode,
                        isTelemetryExpanded = isTelemetryBarExpanded,
                        isHapticsEnabled = isHapticsEnabled,
                        lastHapticFeedbackLog = lastHapticFeedbackLog,
                        onBack = { viewModel.openConversation(null) },
                        onSendMessage = { text ->
                            viewModel.sendTextMessage(activeConversation.id, text)
                        },
                        onSelectReplyMessage = viewModel::setReplyingToMessage,
                        onSimulateLiveFileTransfer = viewModel::simulateLiveQuickShareTransfer,
                        onSelectSelfDestructTimer = viewModel::setSelfDestructTimerSeconds,
                        onSendStealthFile = { name, mime, bytes ->
                            viewModel.sendStealthMeshDropFile(activeConversation.id, name, mime, bytes)
                        },
                        onCapturePrivacyPhoto = { scene, faces, blur ->
                            viewModel.captureAndSendPrivacyEvidencePhoto(activeConversation.id, scene, faces, blur)
                        },
                        onScheduleSms = { text, sim, delayMins ->
                            viewModel.scheduleSmsWithWorkManager(
                                conversationId = activeConversation.id,
                                recipientName = activeConversation.customNickname,
                                messageText = text,
                                simSlot = sim,
                                delayMinutes = delayMins
                            )
                        },
                        onUpdatePeerNickname = { newName ->
                            viewModel.updateCustomPeerNickname(activeConversation.id, newName)
                        },
                        onToggleSimSlot = {
                            viewModel.toggleSimSlot(activeConversation.id)
                        },
                        onRotateSignalRatchet = {
                            viewModel.rotateConversationRatchet(activeConversation.id)
                        },
                        onToggleVerifySafetyNumber = {
                            viewModel.toggleVerifyPeerSafetyNumber(activeConversation.id)
                        },
                        onToggleTelemetryExpanded = viewModel::toggleTelemetryBarExpanded,
                        onCycleFontScale = viewModel::cycleFontScalePreset,
                        onSelectFontScale = viewModel::setFontScalePreset,
                        onCycleContrastMode = viewModel::cycleAdaptiveContrastMode,
                        onSelectContrastMode = viewModel::setAdaptiveContrastMode,
                        onToggleHaptics = viewModel::toggleHapticsEnabled
                    )
                } else {
                    when (selectedTab) {
                        MainNavTab.MESSAGES -> {
                            ConversationInboxScreen(
                                conversations = conversations,
                                searchQuery = searchQuery,
                                isDuressDecoyMode = isDuressDecoyMode,
                                radioGear = radioGear,
                                peerCount = meshPeers.size,
                                isExclusiveOfflineLocked = isExclusiveOfflineMeshLocked,
                                selectedSimSlot = selectedSimSlot,
                                localContacts = localContacts,
                                fontScalePreset = fontScalePreset,
                                adaptiveContrastMode = adaptiveContrastMode,
                                isTelemetryExpanded = isTelemetryBarExpanded,
                                isHapticsEnabled = isHapticsEnabled,
                                lastHapticFeedbackLog = lastHapticFeedbackLog,
                                lastTrashedConversation = lastTrashedConversation,
                                onSearchChange = viewModel::updateSearchQuery,
                                onSelectConversation = viewModel::openConversation,
                                onSwipePinConversation = viewModel::swipePinConversation,
                                onSwipeTrashConversation = viewModel::swipeTrashConversation,
                                onUndoTrashConversation = viewModel::undoTrashConversation,
                                onToggleTelemetryExpanded = viewModel::toggleTelemetryBarExpanded,
                                onCycleFontScale = viewModel::cycleFontScalePreset,
                                onSelectFontScale = viewModel::setFontScalePreset,
                                onCycleContrastMode = viewModel::cycleAdaptiveContrastMode,
                                onSelectContrastMode = viewModel::setAdaptiveContrastMode,
                                onToggleHaptics = viewModel::toggleHapticsEnabled,
                                onToggleSimSlot = { viewModel.toggleSimSlot(null) },
                                onStartChatWithContact = viewModel::startNewMeshChatFromContact,
                                onOpenBurnerTab = { viewModel.selectTab(MainNavTab.BURNER_IDS) },
                                onTriggerPanicPinOrRestore = viewModel::authenticateMultiVaultPin
                            )
                        }

                        MainNavTab.BURNER_IDS -> {
                            BurnerContactsScreen(
                                burnerContacts = burnerContacts,
                                onGenerateBurnerContact = viewModel::generateBurnerContact,
                                onShareBurnerContact = viewModel::shareBurnerContactId,
                                onOpenBurnerChat = { convId ->
                                    viewModel.selectTab(MainNavTab.MESSAGES)
                                    viewModel.openConversation(convId)
                                },
                                onBurnContactImmediately = viewModel::burnContactImmediately
                            )
                        }

                        MainNavTab.MESH_RADAR -> {
                            MeshRadarAndDropScreen(
                                radioGear = radioGear,
                                meshPeers = meshPeers,
                                dtnPacketLogs = dtnPacketLogs,
                                bloomFilterStats = bloomFilterStats,
                                swarmChunks = swarmChunks,
                                isPttTransmitting = isPttTransmitting,
                                pttActiveChannel = pttActiveChannel,
                                pttTransmissions = pttTransmissions,
                                ultrasonicBursts = ultrasonicBursts,
                                aiPredictiveRoutes = aiPredictiveRoutes,
                                satelliteGatewayState = satelliteGatewayState,
                                meshGroupKeys = meshGroupKeys,
                                zkpProofs = zkpProofs,
                                shardedManifests = shardedManifests,
                                selectedSdrMode = selectedSdrMode,
                                sdrBridgeLogs = sdrBridgeLogs,
                                onSimulateMultiHopBounce = {
                                    viewModel.meshEngine.injectSimulatedIncomingRelayBounce()
                                },
                                onToggleLivePtt = viewModel::toggleLivePttTransmission,
                                onBeamUltrasonicBurst = viewModel::beamUltrasonicAcousticBurst,
                                onRunAiPredictiveRouting = viewModel::runAiPredictiveMeshRoutingPass,
                                onToggleSatelliteGateway = viewModel::toggleSatelliteGatewayBridge,
                                onRelaySatellitePacket = viewModel::relayPacketViaSatelliteGateway,
                                onSyncGroupKeyQrNfc = viewModel::generateAndSyncGroupKeyBundle,
                                onGenerateZkpAttestation = viewModel::generateZkpAnonymousGroupAttestation,
                                onShardFileAcrossPeers = viewModel::shardAndDistributeSensitiveFile,
                                onReassembleShardedFile = viewModel::reassembleShardedFileFromSwarm,
                                onSelectSdrMode = viewModel::selectSdrModulationMode,
                                onTransmitSdrPacket = viewModel::transmitSdrHardwareBridgePacket,
                                onOpenPeerChat = { convId ->
                                    viewModel.selectTab(MainNavTab.MESSAGES)
                                    viewModel.openConversation(convId)
                                }
                            )
                        }

                        MainNavTab.TACTICAL_MAP -> {
                            OfflineTacticalMapScreen(
                                pins = tacticalPins,
                                onAddCrowdSosPin = viewModel::addCrowdSosPin,
                                onDeletePin = viewModel::deleteTacticalPin
                            )
                        }

                        MainNavTab.STEALTH_VAULT -> {
                            StealthSecurityVaultScreen(
                                isDuressDecoyMode = isDuressDecoyMode,
                                activeVaultProfile = activeVaultProfile,
                                keyFingerprint = keyFingerprint,
                                signalIdentityFingerprint = signalIdentityFingerprint,
                                signalSignedPreKeyFingerprint = signalSignedPreKeyFingerprint,
                                volumeDownPanicCount = volumeDownPanicCount,
                                isStealthDialerHidden = isStealthDialerHidden,
                                deadManSwitchState = deadManSwitchState,
                                cellTowerReports = cellTowerReports,
                                isExclusiveOfflineMeshLocked = isExclusiveOfflineMeshLocked,
                                gcBackups = gcBackups,
                                simSlots = simSlots,
                                selectedSimSlot = selectedSimSlot,
                                scheduledSmsList = scheduledSmsList,
                                onAuthenticateMultiVaultPin = viewModel::authenticateMultiVaultPin,
                                onTriggerDuressPanic = viewModel::triggerDuressPanic,
                                onSimulateVolumeDownPress = { viewModel.onHardwareVolumeDownPressed() },
                                onRestoreVault = viewModel::restoreFromDuress,
                                onCheckInDeadManSwitch = viewModel::checkInDeadManSwitch,
                                onConfigureDeadManSwitch = viewModel::configureDeadManSwitch,
                                onRunImsiCatcherScan = viewModel::runImsiCatcherCellScan,
                                onEngageExclusiveOfflineMeshLock = viewModel::engageExclusiveOfflineMeshLock,
                                onCreateZeroKnowledgeBackup = viewModel::createZeroKnowledgeBackup,
                                onVerifyRestoreBackup = viewModel::verifyAndRestoreZeroKnowledgeBackup,
                                onToggleStealthDialerHidden = viewModel::toggleStealthDialerVault,
                                onSelectSimSlot = { viewModel.toggleSimSlot(null) },
                                onOpenEmergencyAlerts = viewModel::openSystemEmergencyAlertSettings,
                                onRefreshContacts = viewModel::refreshLocalContacts
                            )
                        }
                    }
                }
            }
        }
    }
}
