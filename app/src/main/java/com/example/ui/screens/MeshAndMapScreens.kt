package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TacticalPinEntity
import com.example.security.CryptoVaultEngine
import com.example.security.SwarmChunkMeta
import com.example.service.DtnPacketLog
import com.example.service.MeshGroupKeyBundle
import com.example.service.MeshPeerNode
import com.example.service.PredictiveRouteCandidate
import com.example.service.PttVoiceTransmission
import com.example.service.RadioGearState
import com.example.service.SatelliteNtnGatewayState
import com.example.service.SdrBridgeFrameLog
import com.example.service.SdrModulationMode
import com.example.service.ShardedFileManifest
import com.example.service.UltrasonicBurstFrame
import com.example.service.ZkpAnonymousAttestationProof
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
import com.example.viewmodel.GcMessagesViewModel

/**
 * Tab 3: Native Offline Mesh & Tactical Radio Hub:
 * - Hybrid Multi-Radio Engine (`Strategy.P2P_CLUSTER` + BLE 5.0 + 5GHz Wi-Fi Direct) & 2048-bit Bloom Filter
 * - Mesh Walkie-Talkie (Live PTT Audio Streaming over BLE + Wi-Fi Direct)
 * - Acoustic / Ultrasonic Data Burst Transmission (18.5 kHz / 19.4 kHz GFSK Audio Modem for Jammed RF)
 * - AI-Driven Predictive Mesh Routing (On-Device Mobility Link-Score Pre-Caching)
 * - Direct-to-Cell Satellite Fallback Bridge (3GPP NTN LEO Open-Sky Gateway Relay)
 * - Mesh QR / NFC 1-Tap E2EE Group Key Distribution & Zero-Knowledge Proof (ZKP) Anonymous Group Attestation
 * - Decentralized Local Sharding (3-of-5 Fragmented Swarm Storage across Trusted Mesh Peers)
 * - SDR & Tactical Radio Hardware Bridge (KISS TNC / AX.25 over VHF 144.390 MHz & UHF 433.920 MHz)
 */
@Composable
fun MeshRadarAndDropScreen(
    radioGear: RadioGearState,
    meshPeers: List<MeshPeerNode>,
    dtnPacketLogs: List<DtnPacketLog>,
    bloomFilterStats: Pair<Int, Int>,
    swarmChunks: List<SwarmChunkMeta>,
    isPttTransmitting: Boolean,
    pttActiveChannel: String,
    pttTransmissions: List<PttVoiceTransmission>,
    ultrasonicBursts: List<UltrasonicBurstFrame>,
    aiPredictiveRoutes: List<PredictiveRouteCandidate>,
    satelliteGatewayState: SatelliteNtnGatewayState,
    meshGroupKeys: List<MeshGroupKeyBundle>,
    zkpProofs: List<ZkpAnonymousAttestationProof>,
    shardedManifests: List<ShardedFileManifest>,
    selectedSdrMode: SdrModulationMode,
    sdrBridgeLogs: List<SdrBridgeFrameLog>,
    onSimulateMultiHopBounce: () -> Unit,
    onToggleLivePtt: () -> Unit,
    onBeamUltrasonicBurst: (String, String) -> Unit,
    onRunAiPredictiveRouting: () -> Unit,
    onToggleSatelliteGateway: (Boolean) -> Unit,
    onRelaySatellitePacket: (String) -> Unit,
    onSyncGroupKeyQrNfc: (String) -> Unit,
    onGenerateZkpAttestation: (String) -> Unit,
    onShardFileAcrossPeers: (String, Long) -> Unit,
    onReassembleShardedFile: (String) -> Unit,
    onSelectSdrMode: (SdrModulationMode) -> Unit,
    onTransmitSdrPacket: (String) -> Unit,
    onOpenPeerChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ultrasonicText by remember { mutableStateOf("SOS RALLY NORTH TUNNEL • KYBER1024") }
    var groupNameInput by remember { mutableStateOf("Alpha Recon Mesh Group") }
    var qrPreviewGroupBundle by remember { mutableStateOf<MeshGroupKeyBundle?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp)
            .testTag("mesh_radar_screen"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. MESH WALKIE-TALKIE (LIVE PTT AUDIO STREAMING)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (isPttTransmitting) DuressRed else WifiDirectGreen.copy(alpha = 0.45f),
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
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isPttTransmitting) DuressRed else WifiDirectGreen,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MESH WALKIE-TALKIE (LIVE PTT)",
                                    color = if (isPttTransmitting) DuressRed else WifiDirectGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pttActiveChannel,
                                    color = PrimaryTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            color = if (isPttTransmitting) DuressRed.copy(alpha = 0.22f) else GraphiteInputPill,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isPttTransmitting) "LIVE ON AIR" else "OPUS 24KBPS",
                                color = if (isPttTransmitting) DuressRed else MeshCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Streams real-time PQXDH encrypted voice audio bursts over BLE 5.0 Extended Advertising & 5GHz Wi-Fi Direct to nearby crowd peers without cellular internet.",
                        color = SecondaryTextGrey,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onToggleLivePtt,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPttTransmitting) DuressRed else OutgoingSamsungTeal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("live_ptt_walkie_talkie_button")
                    ) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPttTransmitting) "STOP & BROADCAST ENCRYPTED PTT BURST" else "PUSH-TO-TALK (LIVE MESH BROADCAST)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    pttTransmissions.take(2).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎙️ ${tx.speakerCallsign} (${tx.durationMillis / 1000.0}s)",
                                color = PrimaryTextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${tx.peakAmplitudeDb} dB • RSSI ${tx.rssiDbm} dBm",
                                color = MeshCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 2. ACOUSTIC / ULTRASONIC DATA BURST TRANSMISSION (18.5 kHz / 19.4 kHz AUDIO MODEM)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalPurple.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SpeakerPhone, contentDescription = null, tint = TacticalPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ACOUSTIC / ULTRASONIC DATA BURST MODEM",
                                color = TacticalPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "18.5 kHz / 19.4 kHz GFSK Air-Gap Audio Link",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When all RF/BLE/Wi-Fi spectrums are jammed, modulates short SOS alerts and PQXDH public keys into near-ultrasonic PCM audio waves beamed via device speaker & microphone.",
                        color = SecondaryTextGrey,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ultrasonicText,
                        onValueChange = { ultrasonicText = it },
                        singleLine = true,
                        label = { Text("Ultrasonic Burst Payload (Max 64B)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onBeamUltrasonicBurst("SOS_BEACON", ultrasonicText) },
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ultrasonic_beam_sos_button")
                        ) {
                            Text("Beam Ultrasonic SOS", color = Color.White, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = { onBeamUltrasonicBurst("PQ_PREKEY_BUNDLE", "KYBER1024_PK_SYNC") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Beam PQC PreKey", color = MeshCyan, fontSize = 11.5.sp)
                        }
                    }
                    ultrasonicBursts.firstOrNull()?.let { latest ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last TX: ${latest.frameId} • ${latest.encodedHexSymbolStream} (${latest.durationMillis}ms)",
                            color = WifiDirectGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3. AI-DRIVEN PREDICTIVE MESH ROUTING & MULTI-HOP DTN ENGINE
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MeshCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MeshCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI-DRIVEN PREDICTIVE MESH ROUTER",
                                    color = MeshCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = radioGear.label,
                                    color = PrimaryTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            color = WifiDirectGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TTL 15 HOPS",
                                color = WifiDirectGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    aiPredictiveRoutes.take(3).forEach { route ->
                        Surface(
                            color = GraphiteInputPill,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = route.nextHopCallsign,
                                        color = PrimaryTextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "AI Score: ${route.deliveryProbabilityPercent}% (${route.preCachedPacketsCount} Pre-Cached)",
                                        color = WifiDirectGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${route.predictedMobilityVector} • ${route.recommendedAction}",
                                    color = SecondaryTextGrey,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onRunAiPredictiveRouting,
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_predictive_route_button")
                        ) {
                            Text("Pre-Cache Optimal Route", color = Color.White, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = onSimulateMultiHopBounce,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_dtn_relay_button")
                        ) {
                            Text("Bloom Dedup (${bloomFilterStats.second})", color = MeshCyan, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // 4. DIRECT-TO-CELL SATELLITE FALLBACK BRIDGE (3GPP NTN LEO GATEWAY)
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
                            Icon(imageVector = Icons.Default.SatelliteAlt, contentDescription = null, tint = BleAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DIRECT-TO-CELL SATELLITE GATEWAY",
                                    color = BleAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = satelliteGatewayState.designatedGatewayNode,
                                    color = PrimaryTextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Switch(
                            checked = satelliteGatewayState.isSatelliteBridgeEnabled,
                            onCheckedChange = onToggleSatelliteGateway,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OutgoingSamsungTeal
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${satelliteGatewayState.constellationName} • ${satelliteGatewayState.openSkyAzimuthElevation} • SNR ${satelliteGatewayState.snrDb} dB • Relayed: ${satelliteGatewayState.uplinkPacketsRelayed} pkts",
                        color = SecondaryTextGrey,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onRelaySatellitePacket("SOS GLOBAL SATELLITE RELAY BEACON") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("satellite_ntn_relay_button")
                    ) {
                        Text("Route Local Mesh Packet via Open-Sky LEO Satellite Gateway", color = BleAmber, fontSize = 11.5.sp)
                    }
                }
            }
        }

        // 5. MESH QR / NFC 1-TAP GROUP KEY DISTRIBUTION & ZERO-KNOWLEDGE PROOF (ZKP) ATTESTATION
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = WifiDirectGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "1-TAP NFC / QR GROUP KEY & ZKP ATTESTATION",
                                color = WifiDirectGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SenderKey Sync + Fiat-Shamir Schnorr ZKP",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        singleLine = true,
                        label = { Text("Mesh Group / ZKP Ring Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSyncGroupKeyQrNfc(groupNameInput)
                                qrPreviewGroupBundle = meshGroupKeys.firstOrNull()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nfc_qr_group_key_button")
                        ) {
                            Text("1-Tap NFC / QR Key", color = Color.White, fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = { onGenerateZkpAttestation(groupNameInput) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("zkp_attestation_button")
                        ) {
                            Text("Verify Anonymous ZKP", color = WifiDirectGreen, fontSize = 11.5.sp)
                        }
                    }
                    zkpProofs.firstOrNull()?.let { proof ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ZKP [${proof.proofId}] Ring=${proof.groupRingId} • Commitment t=${proof.commitmentT} • s=${proof.responseS} -> ${proof.identityRevealed}",
                            color = MeshCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 6. DECENTRALIZED LOCAL SHARDING (FRAGMENTED SWARM STORAGE)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ViewModule, contentDescription = null, tint = MeshCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DECENTRALIZED LOCAL SHARDING (K=3 OF N=5)",
                                color = MeshCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fragmented Swarm Storage Across Trusted Peers",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    shardedManifests.firstOrNull()?.let { manifest ->
                        Text(
                            text = "File: ${manifest.originalFileName} (${GcMessagesViewModel.formatFileSize(manifest.originalSizeBytes)}) • Threshold ${manifest.thresholdK}/${manifest.totalShardsN} Shards",
                            color = PrimaryTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        manifest.shards.forEach { shard ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Shard #${shard.shardIndex}/${shard.totalShardsN} → ${shard.custodianPeerCallsign}",
                                    color = SecondaryTextGrey,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = shard.shardBlake3Hash.take(12),
                                    color = WifiDirectGreen,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onShardFileAcrossPeers("tactical_key_archive_${(10..99).random()}.pmtiles", 18_874_368L) },
                                colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shard_file_swarm_button")
                            ) {
                                Text("Shard New File (3/5)", color = Color.White, fontSize = 11.5.sp)
                            }
                            OutlinedButton(
                                onClick = { onReassembleShardedFile(manifest.manifestId) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reassemble Shards", color = WifiDirectGreen, fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // 7. SDR & TACTICAL RADIO HARDWARE BRIDGE (VHF/UHF KISS TNC AX.25)
        item {
            Surface(
                color = ElevatedCardBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SettingsInputAntenna, contentDescription = null, tint = BleAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SDR & TACTICAL RADIO HARDWARE BRIDGE",
                                color = BleAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedSdrMode.label} (${selectedSdrMode.frequencyMhz})",
                                color = PrimaryTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SdrModulationMode.entries.forEach { mode ->
                            val isSelected = selectedSdrMode == mode
                            Surface(
                                onClick = { onSelectSdrMode(mode) },
                                color = if (isSelected) OutgoingSamsungTeal else GraphiteInputPill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mode.frequencyMhz,
                                    color = if (isSelected) Color.White else SecondaryTextGrey,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onTransmitSdrPacket("VHF/UHF LONG-RANGE MESH RELAY PACKET") },
                        colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sdr_bridge_tx_button")
                    ) {
                        Text("Bridge Mesh Packet over VHF/UHF KISS TNC (AX.25)", color = Color.White, fontSize = 12.sp)
                    }
                    sdrBridgeLogs.firstOrNull()?.let { frame ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${frame.frameId} • ${frame.kissAx25HexHeader}",
                            color = MeshCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 8. 256KB BLAKE3 SWARM CHUNKING & DTN PACKET LOGS
        item {
            Text(
                text = "MULTI-HOP DTN PACKET & BLOOM DEDUPLICATION LOG (${dtnPacketLogs.size})",
                color = TimestampMutedGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(dtnPacketLogs.take(6), key = { it.packetId + it.timestamp }) { log ->
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
                        Text(
                            text = "${log.packetId} • ${log.sourceNode} → ${log.destinationNode}",
                            color = PrimaryTextWhite,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Hop ${log.hopNumber}/${log.maxTtl}",
                            color = TimestampMutedGrey,
                            fontSize = 10.5.sp
                        )
                    }
                    Text(
                        text = log.status,
                        color = if (log.status.contains("DROPPED")) DuressRed else WifiDirectGreen,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    qrPreviewGroupBundle?.let { bundle ->
        AlertDialog(
            onDismissRequest = { qrPreviewGroupBundle = null },
            containerColor = ElevatedCardBg,
            title = {
                Text("1-Tap NFC / Multi-Peer QR Group Key", color = PrimaryTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(bundle.groupName, color = WifiDirectGreen, fontWeight = FontWeight.Bold)
                    Text("SenderKey ID: ${bundle.senderKeyId}", color = MeshCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val grid = 15
                            val cw = size.width / grid
                            val ch = size.height / grid
                            val seed = bundle.qrNfcPayloadUri.hashCode()
                            for (r in 0 until grid) {
                                for (c in 0 until grid) {
                                    val finder = (r < 4 && c < 4) || (r < 4 && c >= grid - 4) || (r >= grid - 4 && c < 4)
                                    val bit = ((seed shr ((r * 5 + c * 7) % 28)) and 1) == 1
                                    if (finder || bit) {
                                        drawRect(Color.Black, Offset(c * cw, r * ch), Size(cw * 0.9f, ch * 0.9f))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NDEF MIME: application/vnd.com.gc.messages.groupkey",
                        color = TimestampMutedGrey,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { qrPreviewGroupBundle = null }) {
                    Text("Done", color = WifiDirectGreen)
                }
            }
        )
    }
}

/**
 * Tab 4: Offline Tactical Map (.pmtiles Vector Tile Engine) for sharing Crowd SOS Pins.
 */
@Composable
fun OfflineTacticalMapScreen(
    pins: List<TacticalPinEntity>,
    onAddCrowdSosPin: (title: String, category: String, gridX: Float, gridY: Float, notes: String) -> Unit,
    onDeletePin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var pendingTapCoord by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    val filteredPins = remember(pins, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") pins
        else pins.filter { it.category == selectedCategoryFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp)
            .testTag("tactical_map_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Offline Tactical Vector Map (.pmtiles)",
                    color = PrimaryTextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Archive: sector7_offline_tactical.pmtiles • Tap map to drop Crowd SOS Pin",
                    color = MeshCyan,
                    fontSize = 11.sp
                )
            }
            IconButton(
                onClick = { pendingTapCoord = Pair(0.50f, 0.50f) },
                modifier = Modifier.testTag("add_sos_pin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = "Drop Crowd SOS Pin",
                    tint = WifiDirectGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "BARRICADE", "FIRST_AID", "SAFE_EXIT", "MESH_RELAY").forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                Surface(
                    onClick = { selectedCategoryFilter = cat },
                    color = if (isSelected) OutgoingSamsungTeal else GraphiteInputPill,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = cat.replace("_", " "),
                        color = if (isSelected) Color.White else SecondaryTextGrey,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF08121A))
                .border(1.dp, MeshCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val normX = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                        val normY = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                        pendingTapCoord = Pair(normX, normY)
                    }
                }
                .testTag("pmtiles_vector_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val gridColor = Color(0xFF13293D)
                for (i in 1..9) {
                    drawLine(gridColor, Offset(w * i / 10f, 0f), Offset(w * i / 10f, h), strokeWidth = 1.2f)
                    drawLine(gridColor, Offset(0f, h * i / 10f), Offset(w, h * i / 10f), strokeWidth = 1.2f)
                }

                val riverPath = Path().apply {
                    moveTo(0f, h * 0.65f)
                    cubicTo(w * 0.35f, h * 0.45f, w * 0.65f, h * 0.75f, w, h * 0.30f)
                }
                drawPath(riverPath, color = Color(0xFF004E89).copy(alpha = 0.55f), style = Stroke(width = 14f))

                val highwayPath = Path().apply {
                    moveTo(w * 0.12f, 0f)
                    lineTo(w * 0.48f, h * 0.52f)
                    lineTo(w * 0.88f, h)
                }
                drawPath(highwayPath, color = Color(0xFF2A475E), style = Stroke(width = 5f))

                for (i in 0 until filteredPins.size - 1) {
                    val p1 = filteredPins[i]
                    val p2 = filteredPins[i + 1]
                    drawLine(
                        color = MeshCyan.copy(alpha = 0.35f),
                        start = Offset(p1.gridX * w, p1.gridY * h),
                        end = Offset(p2.gridX * w, p2.gridY * h),
                        strokeWidth = 2f
                    )
                }

                filteredPins.forEach { pin ->
                    val pinColor = when (pin.category) {
                        "BARRICADE" -> DuressRed
                        "FIRST_AID" -> WifiDirectGreen
                        "SAFE_EXIT" -> MeshCyan
                        else -> BleAmber
                    }
                    val center = Offset(pin.gridX * w, pin.gridY * h)
                    drawCircle(color = pinColor.copy(alpha = 0.28f), radius = 22f, center = center)
                    drawCircle(color = pinColor, radius = 9f, center = center)
                    drawCircle(color = Color.White, radius = 3.5f, center = center)
                }
            }

            Surface(
                color = AmoledBlack.copy(alpha = 0.75f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "PMTiles v3 Vector Zoom 14 • ${filteredPins.size} Active Crowd SOS Pins",
                    color = WifiDirectGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredPins, key = { it.id }) { pin ->
                val notesDecrypted = remember(pin.notesCiphertext) {
                    CryptoVaultEngine.decryptAes256Gcm(pin.notesCiphertext)
                }
                val badgeColor = when (pin.category) {
                    "BARRICADE" -> DuressRed
                    "FIRST_AID" -> WifiDirectGreen
                    "SAFE_EXIT" -> MeshCyan
                    else -> BleAmber
                }
                Surface(
                    color = GraphiteInputPill,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pin.title,
                                    color = PrimaryTextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[${pin.category}]",
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "MGRS: ${pin.mgrsCoord} • By ${pin.reporterCallsign} • TTL ${pin.ttlHops} hops",
                                color = MeshCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = notesDecrypted,
                                color = SecondaryTextGrey,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeletePin(pin.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Pin",
                                tint = TimestampMutedGrey
                            )
                        }
                    }
                }
            }
        }
    }

    pendingTapCoord?.let { (gx, gy) ->
        var title by remember { mutableStateOf("Emergency Crowd SOS Marker") }
        var category by remember { mutableStateOf("BARRICADE") }
        var notes by remember { mutableStateOf("Verified via local visual recon. Broadcast over 15-hop mesh.") }

        AlertDialog(
            onDismissRequest = { pendingTapCoord = null },
            containerColor = ElevatedCardBg,
            title = {
                Text("Broadcast Crowd SOS Pin (.pmtiles)", color = PrimaryTextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        label = { Text("Pin Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BARRICADE", "FIRST_AID", "SAFE_EXIT").forEach { cat ->
                            OutlinedButton(
                                onClick = { category = cat },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = cat.replace("_", " "),
                                    fontSize = 9.sp,
                                    color = if (category == cat) WifiDirectGreen else SecondaryTextGrey
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Encrypted Tactical Notes") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCrowdSosPin(title, category, gx, gy, notes)
                        pendingTapCoord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
                ) {
                    Text("Broadcast to Swarm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingTapCoord = null }) {
                    Text("Cancel", color = TimestampMutedGrey)
                }
            }
        )
    }
}
