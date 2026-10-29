package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BurnerContactEntity
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BleAmber
import com.example.ui.theme.DuressRed
import com.example.ui.theme.ElevatedCardBg
import com.example.ui.theme.GraphiteInputPill
import com.example.ui.theme.MeshCyan
import com.example.ui.theme.OutgoingSamsungTeal
import com.example.ui.theme.PrimaryTextWhite
import com.example.ui.theme.SecondaryTextGrey
import com.example.ui.theme.TimestampMutedGrey
import com.example.ui.theme.WifiDirectGreen
import com.example.viewmodel.GcMessagesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExpirationOption(
    val label: String,
    val durationMinutes: Long? // null = Never expires until manual burn
)

private val BURNER_EXPIRATION_OPTIONS = listOf(
    ExpirationOption("15 Mins", 15L),
    ExpirationOption("1 Hour", 60L),
    ExpirationOption("24 Hours", 1440L),
    ExpirationOption("7 Days", 10080L),
    ExpirationOption("No Expiry", null)
)

/**
 * Dedicated Burner Contacts Screen:
 * - Generates anonymous, temporary contact IDs (`BRN-XXXX-XXXX-XXXX`) that can be shared via QR / Token URI
 * - Strictly isolated from the device's main `ContactsContract` contact list
 * - Supports optional expiration dates with automatic self-destruction of E2EE keys and chat history
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BurnerContactsScreen(
    burnerContacts: List<BurnerContactEntity>,
    onGenerateBurnerContact: (aliasNickname: String, expirationMinutes: Long?) -> Unit,
    onShareBurnerContact: (BurnerContactEntity) -> Unit,
    onOpenBurnerChat: (String) -> Unit,
    onBurnContactImmediately: (BurnerContactEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateBurnerDialog by remember { mutableStateOf(false) }
    var qrPreviewBurner by remember { mutableStateOf<BurnerContactEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp)
            .testTag("burner_contacts_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Isolation & Signal E2EE Banner
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = BleAmber,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ANONYMOUS BURNER CONTACTS",
                                color = BleAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isolated Ephemeral Identities",
                                color = PrimaryTextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        color = WifiDirectGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ZERO OS SYNC",
                            color = WifiDirectGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generate disposable anonymous IDs (BRN-XXXX-XXXX-XXXX) backed by ephemeral Signal X3DH PreKeys. Burner contacts are stored in an encrypted sandbox completely separate from Android ContactsContract and auto-shred upon expiration.",
                    color = SecondaryTextGrey,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showCreateBurnerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_burner_id_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Anonymous Burner Contact ID", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE BURNER IDENTITIES (${burnerContacts.size})",
                color = TimestampMutedGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Isolated from Device Contacts",
                color = MeshCyan,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("burner_contacts_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(burnerContacts, key = { it.id }) { burner ->
                BurnerContactCard(
                    burner = burner,
                    onOpenChat = { onOpenBurnerChat(burner.linkedConversationId) },
                    onShare = { onShareBurnerContact(burner) },
                    onShowQr = { qrPreviewBurner = burner },
                    onBurnNow = { onBurnContactImmediately(burner) }
                )
            }
        }
    }

    if (showCreateBurnerDialog) {
        var aliasInput by remember { mutableStateOf("Field-Drop-${(100..999).random()}") }
        var selectedOption by remember { mutableStateOf(BURNER_EXPIRATION_OPTIONS[2]) } // Default 24 Hours

        AlertDialog(
            onDismissRequest = { showCreateBurnerDialog = false },
            containerColor = ElevatedCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = BleAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Anonymous Burner ID", color = PrimaryTextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Generates a cryptographically random Burner ID and fresh Signal X3DH PreKey bundle. Never touches your device contacts:",
                        color = SecondaryTextGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = aliasInput,
                        onValueChange = { aliasInput = it },
                        singleLine = true,
                        label = { Text("Temporary Alias Label") },
                        modifier = Modifier.testTag("burner_alias_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Optional Expiration Date / Self-Destruct Timer:",
                        color = MeshCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BURNER_EXPIRATION_OPTIONS.forEach { option ->
                            val isSelected = selectedOption == option
                            Surface(
                                onClick = { selectedOption = option },
                                color = if (isSelected) OutgoingSamsungTeal else GraphiteInputPill,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("expiry_option_${option.label}")
                            ) {
                                Text(
                                    text = option.label,
                                    color = if (isSelected) Color.White else SecondaryTextGrey,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onGenerateBurnerContact(aliasInput, selectedOption.durationMinutes)
                        showCreateBurnerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                    modifier = Modifier.testTag("confirm_create_burner_button")
                ) {
                    Text("Create Burner ID", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBurnerDialog = false }) {
                    Text("Cancel", color = TimestampMutedGrey)
                }
            }
        )
    }

    qrPreviewBurner?.let { burner ->
        BurnerShareQrDialog(
            burner = burner,
            onShareSystemSheet = {
                qrPreviewBurner = null
                onShareBurnerContact(burner)
            },
            onDismiss = { qrPreviewBurner = null }
        )
    }
}

@Composable
private fun BurnerContactCard(
    burner: BurnerContactEntity,
    onOpenChat: () -> Unit,
    onShare: () -> Unit,
    onShowQr: () -> Unit,
    onBurnNow: () -> Unit
) {
    val now = System.currentTimeMillis()
    val expirationLabel = remember(burner.expiresAt) {
        if (burner.expiresAt == null) {
            "No Expiration (Manual Burn Only)"
        } else {
            val remainingMins = ((burner.expiresAt - now) / 60_000L).coerceAtLeast(1L)
            val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(burner.expiresAt))
            "Expires in ${GcMessagesViewModel.formatDurationMinutes(remainingMins)} ($dateStr)"
        }
    }

    Surface(
        color = GraphiteInputPill,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("burner_card_${burner.anonymousBurnerId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BleAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = BleAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = burner.aliasNickname,
                            color = PrimaryTextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = burner.anonymousBurnerId,
                            color = MeshCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Expiration Badge
                Surface(
                    color = if (burner.expiresAt != null) BleAmber.copy(alpha = 0.18f) else OutgoingSamsungTeal.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (burner.expiresAt != null) BleAmber else WifiDirectGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (burner.expiresAt != null) "TIMED BURN" else "PERSISTENT",
                            color = if (burner.expiresAt != null) BleAmber else WifiDirectGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = expirationLabel,
                color = SecondaryTextGrey,
                fontSize = 11.sp
            )
            Text(
                text = "Share Token: ${burner.shareableTokenUri}",
                color = TimestampMutedGrey,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Signal Safety Number: ${burner.signalSafetyNumberSnippet}...",
                color = WifiDirectGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenChat,
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("E2EE Chat", color = Color.White, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_burner_${burner.anonymousBurnerId}")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Burner ID", tint = MeshCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share ID", color = MeshCyan, fontSize = 12.sp)
                }

                IconButton(
                    onClick = onShowQr,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(imageVector = Icons.Default.QrCode2, contentDescription = "Show Burner QR Matrix", tint = PrimaryTextWhite)
                }

                IconButton(
                    onClick = onBurnNow,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("burn_now_${burner.anonymousBurnerId}")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Burn & Destroy Now", tint = DuressRed)
                }
            }
        }
    }
}

@Composable
private fun BurnerShareQrDialog(
    burner: BurnerContactEntity,
    onShareSystemSheet: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedCardBg,
        title = {
            Text("Share Anonymous Burner ID", color = PrimaryTextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = burner.anonymousBurnerId,
                    color = MeshCyan,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Deterministic QR Matrix representation of the Burner Share Token URI
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val grid = 15
                        val cellW = size.width / grid
                        val cellH = size.height / grid
                        val seed = burner.shareableTokenUri.hashCode()
                        for (r in 0 until grid) {
                            for (c in 0 until grid) {
                                val isCornerFinder = (r < 4 && c < 4) || (r < 4 && c >= grid - 4) || (r >= grid - 4 && c < 4)
                                val bit = ((seed shr ((r * 7 + c * 3) % 28)) and 1) == 1
                                if (isCornerFinder || bit) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * cellW, r * cellH),
                                        size = Size(cellW * 0.92f, cellH * 0.92f)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = burner.shareableTokenUri,
                    color = SecondaryTextGrey,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onShareSystemSheet,
                colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Token & Copy", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TimestampMutedGrey)
            }
        }
    )
}
