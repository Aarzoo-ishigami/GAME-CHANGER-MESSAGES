package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduledSmsEntity
import com.example.security.CryptoVaultEngine
import com.example.security.SignalProtocolEngine
import com.example.service.ActiveVaultProfile
import com.example.service.CellTowerAnomalyReport
import com.example.service.DeadManSwitchState
import com.example.service.GcBackupArchiveMeta
import com.example.service.SimCardSlotInfo
import com.example.service.ThreatSeverity
import com.example.ui.components.GcMessagesAppLogo
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BleAmber
import com.example.ui.theme.DuressRed
import com.example.ui.theme.ElevatedCardBg
import com.example.ui.theme.GraphiteInputPill
import com.example.ui.theme.MeshCyan
import com.example.ui.theme.OutgoingSamsungTeal
import com.example.ui.theme.PrimaryTextWhite
import com.example.ui.theme.SecondaryTextGrey
import com.example.ui.theme.TacticalPurple
import com.example.ui.theme.TimestampMutedGrey
import com.example.ui.theme.WifiDirectGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StealthSecurityVaultScreen(
    isDuressDecoyMode: Boolean,
    activeVaultProfile: ActiveVaultProfile,
    keyFingerprint: String,
    signalIdentityFingerprint: String,
    signalSignedPreKeyFingerprint: String,
    volumeDownPanicCount: Int,
    isStealthDialerHidden: Boolean,
    deadManSwitchState: DeadManSwitchState,
    cellTowerReports: List<CellTowerAnomalyReport>,
    isExclusiveOfflineMeshLocked: Boolean,
    gcBackups: List<GcBackupArchiveMeta>,
    simSlots: List<SimCardSlotInfo>,
    selectedSimSlot: Int,
    scheduledSmsList: List<ScheduledSmsEntity>,
    onAuthenticateMultiVaultPin: (String) -> Unit,
    onTriggerDuressPanic: (String) -> Unit,
    onSimulateVolumeDownPress: () -> Unit,
    onRestoreVault: (String) -> Unit,
    onCheckInDeadManSwitch: () -> Unit,
    onConfigureDeadManSwitch: (Boolean, Int) -> Unit,
    onRunImsiCatcherScan: (Boolean) -> Unit,
    onEngageExclusiveOfflineMeshLock: (Boolean) -> Unit,
    onCreateZeroKnowledgeBackup: (String) -> Unit,
    onVerifyRestoreBackup: (GcBackupArchiveMeta, String) -> Unit,
    onToggleStealthDialerHidden: (Boolean) -> Unit,
    onSelectSimSlot: () -> Unit,
    onOpenEmergencyAlerts: () -> Unit,
    onRefreshContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinPadInput by remember { mutableStateOf("") }
    var backupPassword by remember { mutableStateOf("QuantumVault#2026") }
    var forceVectorLogoPreview by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp)
            .testTag("stealth_vault_screen"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. POST-QUANTUM SIGNAL E2EE (CRYSTALS-KYBER-1024 + DILITHIUM-5) & ANDROID KEYSTORE ENCLAVE
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MeshCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EnhancedEncryption,
                            contentDescription = null,
                            tint = MeshCyan,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "POST-QUANTUM SIGNAL E2EE (PQXDH + SPQR)",
                                color = MeshCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CRYSTALS-Kyber-1024 + Dilithium-5",
                                color = PrimaryTextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Identity & Lattice KEM Key:\n$signalIdentityFingerprint",
                        color = WifiDirectGreen,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signed PreKey & Dilithium-5:\n$signalSignedPreKeyFingerprint",
                        color = SecondaryTextGrey,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. MULTI-VAULT / DUAL-PASSWORD HIDDEN PROFILES & DURESS PANIC WIPE
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isDuressDecoyMode) DuressRed else WifiDirectGreen.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDuressDecoyMode) Icons.Default.GppBad else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isDuressDecoyMode) DuressRed else WifiDirectGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MULTI-VAULT / DUAL-PASSWORD PROFILES",
                                    color = if (isDuressDecoyMode) DuressRed else WifiDirectGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeVaultProfile.title,
                                    color = PrimaryTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            color = GraphiteInputPill,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = activeVaultProfile.badge,
                                color = MeshCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Primary PIN (1234#): Real Encrypted Tactical Profile\n• Secondary PIN (5555#): Harmless Decoy Profile\n• Duress Panic PIN (9999# / 5x Vol-Down): Instant RAM Shred",
                        color = SecondaryTextGrey,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onAuthenticateMultiVaultPin("1234#") },
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vault_pin_primary_1234")
                        ) {
                            Text("1234# Real", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onAuthenticateMultiVaultPin("5555#") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vault_pin_decoy_5555")
                        ) {
                            Text("5555# Decoy", color = BleAmber, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onTriggerDuressPanic("Duress Panic PIN 9999#") },
                            colors = ButtonDefaults.buttonColors(containerColor = DuressRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_duress_panic_button")
                        ) {
                            Text("9999# Wipe", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSimulateVolumeDownPress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hardware_vol_down_panic_button")
                    ) {
                        Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = BleAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hardware Volume-Down Panic (${volumeDownPanicCount}/5)", color = BleAmber, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. AUTOMATED DEAD MAN'S SWITCH (INACTIVITY KEY SHREDDER)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BleAmber.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.TimerOff, contentDescription = null, tint = BleAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AUTOMATED DEAD MAN'S SWITCH",
                                    color = BleAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Inactivity Threshold: ${deadManSwitchState.timeoutHours} Hours",
                                    color = PrimaryTextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Switch(
                            checked = deadManSwitchState.isArmed,
                            onCheckedChange = { armed -> onConfigureDeadManSwitch(armed, deadManSwitchState.timeoutHours) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OutgoingSamsungTeal
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "If operator fails to check in within ${deadManSwitchState.timeoutHours}h, automatically shreds all local encryption keys and wipes chats.",
                        color = SecondaryTextGrey,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(24, 48, 72).forEach { hrs ->
                            val isSelected = deadManSwitchState.timeoutHours == hrs
                            Surface(
                                onClick = { onConfigureDeadManSwitch(true, hrs) },
                                color = if (isSelected) OutgoingSamsungTeal else GraphiteInputPill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${hrs}h Timer",
                                    color = if (isSelected) Color.White else SecondaryTextGrey,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onCheckInDeadManSwitch,
                        colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dead_man_checkin_button")
                    ) {
                        Text("Operator Check-In (Reset ${deadManSwitchState.timeoutHours}h Countdown)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. IMSI CATCHER & FAKE CELL TOWER (STINGRAY) DETECTOR
        item {
            val hasStingrayAlert = cellTowerReports.any { it.severity == ThreatSeverity.STINGRAY_DETECTED_CRITICAL }
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (hasStingrayAlert) DuressRed else MeshCyan.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = if (hasStingrayAlert) DuressRed else WifiDirectGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IMSI CATCHER & STINGRAY DETECTOR",
                                color = if (hasStingrayAlert) DuressRed else WifiDirectGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasStingrayAlert) "ROGUE CELL SIMULATOR ALERT!" else "Cellular Baseband Monitor Clear",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    cellTowerReports.take(2).forEach { report ->
                        Surface(
                            color = GraphiteInputPill,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${report.cellIdentityCid} • ${report.radioGeneration} (${report.rssiDbm} dBm)",
                                    color = if (report.severity == ThreatSeverity.STINGRAY_DETECTED_CRITICAL) DuressRed else PrimaryTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cipher: ${report.cipheringIndicator} • TAC ${report.trackingAreaCodeTac}",
                                    color = MeshCyan,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = report.findingSummary,
                                    color = SecondaryTextGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onRunImsiCatcherScan(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_stingray_scan_button")
                        ) {
                            Text("Test Stingray Alert", color = DuressRed, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onEngageExclusiveOfflineMeshLock(!isExclusiveOfflineMeshLocked) },
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("offline_mesh_lock_button")
                        ) {
                            Text(
                                text = if (isExclusiveOfflineMeshLocked) "Mesh Lock: ON" else "Lock Offline Mesh",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. ENCRYPTED ZERO-KNOWLEDGE LOCAL BACKUPS (.gcbackup)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = TacticalPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ZERO-KNOWLEDGE LOCAL BACKUPS (.gcbackup)",
                                color = TacticalPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PBKDF2-HMAC-SHA256 (60k) + AES-256-GCM",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        singleLine = true,
                        label = { Text("Zero-Knowledge Backup Passphrase") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onCreateZeroKnowledgeBackup(backupPassword) },
                        colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_gcbackup_button")
                    ) {
                        Text("Export Encrypted .gcbackup Archive", color = Color.White, fontSize = 12.sp)
                    }

                    gcBackups.firstOrNull()?.let { latestBackup ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = GraphiteInputPill,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(latestBackup.fileName, color = PrimaryTextWhite, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${latestBackup.sizeBytes}B • ${latestBackup.sha256Checksum}",
                                        color = WifiDirectGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                OutlinedButton(onClick = { onVerifyRestoreBackup(latestBackup, backupPassword) }) {
                                    Text("Restore", color = MeshCyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. STEALTH DIALER VAULT (*#*#1234#*#*)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = MeshCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Stealth Dialer Vault (*#*#1234#*#*)",
                                    color = PrimaryTextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hide launcher alias & summon via Android Secret Dialer Receiver",
                                    color = TimestampMutedGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = isStealthDialerHidden,
                            onCheckedChange = onToggleStealthDialerHidden,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OutgoingSamsungTeal
                            ),
                            modifier = Modifier.testTag("stealth_launcher_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pinPadInput,
                            onValueChange = { pinPadInput = it },
                            singleLine = true,
                            label = { Text("Dialer PIN (1234#, 5555#, or 9999#)") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                onAuthenticateMultiVaultPin(pinPadInput.ifBlank { "1234#" })
                                pinPadInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
                        ) {
                            Icon(imageVector = Icons.Default.Dialpad, contentDescription = "Execute Dial Code")
                        }
                    }
                }
            }
        }

        // 7. DUAL-SIM SELECTION, CONTACTS CONTRACT & EMERGENCY ALERTS
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SimCard, contentDescription = null, tint = OutgoingSamsungTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dual-SIM & Emergency Alert Integration",
                            color = PrimaryTextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenEmergencyAlerts,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emergency_alerts_settings_button")
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = BleAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Alerts", color = PrimaryTextWhite, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = onRefreshContacts,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sync ContactsContract", color = MeshCyan, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // 8. APP LOGO ASSET (`assets/1000086219.png`) & VECTOR FALLBACK INSPECTOR
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        GcMessagesAppLogo(
                            size = 48.dp,
                            forceProceduralVector = forceVectorLogoPreview
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Logo & Vector Fallback",
                                color = PrimaryTextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (forceVectorLogoPreview) "Showing: Procedural Vector Painter" else "Showing: assets/1000086219.png",
                                color = MeshCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                    OutlinedButton(onClick = { forceVectorLogoPreview = !forceVectorLogoPreview }) {
                        Text(if (forceVectorLogoPreview) "Use PNG" else "Test Vector", fontSize = 11.sp, color = PrimaryTextWhite)
                    }
                }
            }
        }
    }
}
