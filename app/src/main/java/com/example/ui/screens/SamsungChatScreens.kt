package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationEntity
import com.example.data.MessageEntity
import com.example.security.CryptoVaultEngine
import com.example.security.SignalSessionSummary
import com.example.service.AdaptiveContrastMode
import com.example.service.FontScalePreset
import com.example.service.LocalContactEntry
import com.example.service.RadioGearState
import com.example.service.TacticalHapticEngine
import com.example.ui.components.ContextualTelemetryBar
import com.example.ui.components.FluidProgressRingFileCard
import com.example.ui.components.GcMessagesAppLogo
import com.example.ui.components.SwipeToReplyBubbleWrapper
import com.example.ui.components.SwipeableConversationRowWrapper
import com.example.ui.theme.BleAmber
import com.example.ui.theme.DividerSubtle
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
import com.example.viewmodel.StealthFileDropPreset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationInboxScreen(
    conversations: List<ConversationEntity>,
    searchQuery: String,
    isDuressDecoyMode: Boolean,
    radioGear: RadioGearState,
    peerCount: Int,
    isExclusiveOfflineLocked: Boolean,
    selectedSimSlot: Int,
    localContacts: List<LocalContactEntry>,
    fontScalePreset: FontScalePreset,
    adaptiveContrastMode: AdaptiveContrastMode,
    isTelemetryExpanded: Boolean,
    isHapticsEnabled: Boolean,
    lastHapticFeedbackLog: String,
    lastTrashedConversation: ConversationEntity?,
    onSearchChange: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onSwipePinConversation: (String) -> Unit,
    onSwipeTrashConversation: (String) -> Unit,
    onUndoTrashConversation: () -> Unit,
    onToggleTelemetryExpanded: () -> Unit,
    onCycleFontScale: () -> Unit,
    onSelectFontScale: (FontScalePreset) -> Unit,
    onCycleContrastMode: () -> Unit,
    onSelectContrastMode: (AdaptiveContrastMode) -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleSimSlot: () -> Unit,
    onStartChatWithContact: (LocalContactEntry) -> Unit,
    onOpenBurnerTab: () -> Unit,
    onTriggerPanicPinOrRestore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showContactsSheet by remember { mutableStateOf(false) }
    val palette = remember(adaptiveContrastMode) {
        TacticalHapticEngine.resolvePalette(adaptiveContrastMode)
    }
    val scale = fontScalePreset.scaleFactor

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { conv ->
            val decryptedPreview = if (conv.isDecoyRecord) conv.lastMessagePreviewCipher
            else CryptoVaultEngine.decryptAes256Gcm(conv.lastMessagePreviewCipher, conv.id)
            conv.customNickname.contains(searchQuery, ignoreCase = true) ||
                conv.quickShareProfileName.contains(searchQuery, ignoreCase = true) ||
                decryptedPreview.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Samsung One UI Messages Header with Instant Typography & Contrast Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GcMessagesAppLogo(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isDuressDecoyMode) "Messages" else "GC Messages",
                            color = palette.primaryTextColor,
                            fontSize = (22f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isDuressDecoyMode) {
                                "SIM $selectedSimSlot • Standard SMS"
                            } else {
                                "PQXDH + Kyber-1024 • ${radioGear.label}"
                            },
                            color = if (isDuressDecoyMode) palette.timestampTextColor else palette.accentCyanColor,
                            fontSize = (10.5f * scale).sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Instant Dynamic Font-Size Quick Toggle Button (A / A+ / A++)
                    Surface(
                        onClick = onCycleFontScale,
                        shape = RoundedCornerShape(16.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("font_scale_quick_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Toggle Font Size",
                                tint = palette.accentCyanColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = fontScalePreset.shortBadge,
                                color = palette.primaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Instant Adaptive Contrast Quick Toggle Button (AMOLED / HI-CON / SUNLIGHT)
                    Surface(
                        onClick = onCycleContrastMode,
                        shape = RoundedCornerShape(16.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("contrast_mode_quick_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Toggle Sunlight or High-Contrast Mode",
                                tint = BleAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = adaptiveContrastMode.shortBadge,
                                color = palette.primaryTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Burner ID Chip (Hidden in Decoy Mode for zero suspicion)
                    if (!isDuressDecoyMode) {
                        Surface(
                            onClick = onOpenBurnerTab,
                            shape = RoundedCornerShape(18.dp),
                            color = BleAmber.copy(alpha = 0.18f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("quick_burner_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Burner Contacts",
                                    tint = BleAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Burner",
                                    color = BleAmber,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Dual-SIM Selector Pill (In Decoy mode, long-press or tap SIM pill also allows discreet 1234# restore)
                    Surface(
                        onClick = onToggleSimSlot,
                        shape = RoundedCornerShape(18.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sim_selector_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = "Switch Active Dual-SIM Slot",
                                tint = OutgoingSamsungTeal,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SIM $selectedSimSlot",
                                color = palette.primaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Minimalist Collapsible Contextual Telemetry Bar beneath the App Bar
            // Hidden in Decoy Mode so the Decoy SMS Inbox looks 100% like an ordinary SMS client
            if (!isDuressDecoyMode) {
                ContextualTelemetryBar(
                    isExpanded = isTelemetryExpanded,
                    peerCount = peerCount,
                    radioGear = radioGear,
                    isExclusiveOfflineLocked = isExclusiveOfflineLocked,
                    encryptionHealthLabel = "PQXDH + AES-256",
                    fontScalePreset = fontScalePreset,
                    adaptiveContrastMode = adaptiveContrastMode,
                    palette = palette,
                    isHapticsEnabled = isHapticsEnabled,
                    lastHapticFeedbackLog = lastHapticFeedbackLog,
                    onToggleExpand = onToggleTelemetryExpanded,
                    onSelectFontScale = onSelectFontScale,
                    onSelectContrastMode = onSelectContrastMode,
                    onToggleHaptics = onToggleHaptics
                )
            }

            // Samsung Dark Graphite Search Bar (Supports instant 0ms PIN entry: 9999# Panic, 5555# Decoy, 1234# Restore)
            Surface(
                color = palette.inputPillColor,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search conversations or enter PIN",
                        tint = palette.timestampTextColor,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { newVal ->
                            if (newVal == "9999#" || newVal == "5555#" || newVal == "1234#") {
                                onTriggerPanicPinOrRestore(newVal)
                                onSearchChange("")
                            } else {
                                onSearchChange(newVal)
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = palette.primaryTextColor, fontSize = (14f * scale).sp),
                        cursorBrush = SolidColor(OutgoingSamsungTeal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inbox_search_input"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isDuressDecoyMode) {
                                        "Search messages..."
                                    } else {
                                        "Search E2EE chats, Burner IDs, or type 9999# panic..."
                                    },
                                    color = palette.timestampTextColor,
                                    fontSize = (12.5f * scale).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (isDuressDecoyMode) {
                        // Subtle, innocuous carrier sync chip that restores the primary vault on tap for effortless testing
                        Surface(
                            onClick = { onTriggerPanicPinOrRestore("1234#") },
                            color = palette.elevatedSurfaceColor,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("decoy_restore_chip")
                        ) {
                            Text(
                                text = "RCS Sync",
                                color = palette.timestampTextColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = palette.timestampTextColor,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onSearchChange("") }
                        )
                    }
                }
            }

            // Gesture Hint + Undo Trash Bar
            if (!isDuressDecoyMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⇄ Swipe Right to Pin • Swipe Left to Trash • Numbers Masked",
                        color = palette.timestampTextColor,
                        fontSize = (10.5f * scale).sp
                    )
                    if (lastTrashedConversation != null) {
                        Surface(
                            onClick = onUndoTrashConversation,
                            color = OutgoingSamsungTeal.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("undo_trash_conversation_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Undo,
                                    contentDescription = "Undo Trash",
                                    tint = palette.accentGreenColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UNDO TRASH",
                                    color = palette.accentGreenColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "TTL: 15 Hops",
                            color = palette.accentGreenColor,
                            fontSize = (10.5f * scale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("conversation_list"),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conversation ->
                    SwipeableConversationRowWrapper(
                        conversationId = conversation.id,
                        isPinned = conversation.isPinned,
                        palette = palette,
                        onSwipePin = { onSwipePinConversation(conversation.id) },
                        onSwipeTrash = { onSwipeTrashConversation(conversation.id) }
                    ) {
                        ConversationRowItem(
                            conversation = conversation,
                            fontScale = scale,
                            palette = palette,
                            onClick = { onSelectConversation(conversation.id) },
                            onQuickPin = { onSwipePinConversation(conversation.id) },
                            onQuickTrash = { onSwipeTrashConversation(conversation.id) }
                        )
                    }
                    HorizontalDivider(
                        color = DividerSubtle.copy(alpha = 0.55f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp)
                    )
                }
            }
        }

        // Floating Action Button for Local Contacts (`ContactsContract`) or Burner Contacts
        FloatingActionButton(
            onClick = { showContactsSheet = true },
            containerColor = OutgoingSamsungTeal,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_chat_contacts_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Stealth Mesh / ContactsContract Chat"
            )
        }
    }

    if (showContactsSheet) {
        ContactsContractPickerDialog(
            contacts = localContacts,
            onSelectContact = { contact ->
                showContactsSheet = false
                onStartChatWithContact(contact)
            },
            onOpenBurnerScreen = {
                showContactsSheet = false
                onOpenBurnerTab()
            },
            onDismiss = { showContactsSheet = false }
        )
    }
}

@Composable
private fun ConversationRowItem(
    conversation: ConversationEntity,
    fontScale: Float,
    palette: com.example.service.AdaptiveContrastPalette,
    onClick: () -> Unit,
    onQuickPin: () -> Unit,
    onQuickTrash: () -> Unit
) {
    val previewPlain = remember(conversation.lastMessagePreviewCipher, conversation.isDecoyRecord) {
        if (conversation.isDecoyRecord) conversation.lastMessagePreviewCipher
        else CryptoVaultEngine.decryptAes256Gcm(conversation.lastMessagePreviewCipher, conversation.id)
    }
    val timeLabel = remember(conversation.lastMessageTimestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(conversation.lastMessageTimestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_row_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            conversation.isBurnerConversation -> BleAmber.copy(alpha = 0.22f)
                            conversation.isMeshPeer -> OutgoingSamsungTeal.copy(alpha = 0.25f)
                            else -> palette.inputPillColor
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.customNickname.replace("🔥 ", "").take(2).uppercase(),
                    color = when {
                        conversation.isBurnerConversation -> BleAmber
                        conversation.isMeshPeer -> palette.accentCyanColor
                        else -> palette.primaryTextColor
                    },
                    fontSize = (15.5f * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            conversation.isBurnerConversation -> BleAmber
                            conversation.currentTransport.contains("5GHz") -> palette.accentGreenColor
                            conversation.isMeshPeer -> palette.accentCyanColor
                            else -> BleAmber
                        }
                    )
                    .border(2.dp, palette.backgroundColor, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned Conversation",
                            tint = palette.accentGreenColor,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    }
                    Text(
                        text = conversation.customNickname,
                        color = palette.primaryTextColor,
                        fontSize = (15.5f * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeLabel,
                    color = palette.timestampTextColor,
                    fontSize = (11.5f * fontScale).sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (!conversation.isDecoyRecord) {
                Text(
                    text = when {
                        conversation.isBurnerConversation -> {
                            "Anonymous Burner ID • Isolated E2EE (${conversation.currentTransport})"
                        }
                        conversation.isMeshPeer -> {
                            "Signal E2EE • ${conversation.quickShareProfileName} • ${conversation.hopDistance} hops"
                        }
                        else -> {
                            "Signal E2EE • ${conversation.maskedPhoneHint} • ${conversation.currentTransport}"
                        }
                    },
                    color = when {
                        conversation.isBurnerConversation -> BleAmber
                        conversation.isMeshPeer -> palette.accentCyanColor
                        else -> palette.timestampTextColor
                    },
                    fontSize = (10.5f * fontScale).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = previewPlain,
                    color = if (conversation.unreadCount > 0) palette.primaryTextColor else palette.secondaryTextColor,
                    fontSize = (13f * fontScale).sp,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(OutgoingSamsungTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Accessible 1-tap Pin & Trash quick actions alongside swipe gestures
                    if (!conversation.isDecoyRecord) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (conversation.isPinned) "Unpin Conversation" else "Pin Conversation",
                            tint = if (conversation.isPinned) palette.accentGreenColor else palette.timestampTextColor.copy(alpha = 0.55f),
                            modifier = Modifier
                                .size(17.dp)
                                .clickable { onQuickPin() }
                                .testTag("pin_conv_${conversation.id}")
                        )
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Trash Conversation",
                            tint = palette.timestampTextColor.copy(alpha = 0.55f),
                            modifier = Modifier
                                .size(17.dp)
                                .clickable { onQuickTrash() }
                                .testTag("trash_conv_${conversation.id}")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SamsungChatThreadScreen(
    conversation: ConversationEntity,
    messages: List<MessageEntity>,
    radioGear: RadioGearState,
    peerCount: Int,
    isExclusiveOfflineLocked: Boolean,
    selectedSimSlot: Int,
    selectedSelfDestructSeconds: Long?,
    selfDestructPresets: List<com.example.viewmodel.SelfDestructTimerPreset>,
    signalSessionSummary: SignalSessionSummary?,
    anyFormatPresets: List<StealthFileDropPreset>,
    replyingToMessage: MessageEntity?,
    fontScalePreset: FontScalePreset,
    adaptiveContrastMode: AdaptiveContrastMode,
    isTelemetryExpanded: Boolean,
    isHapticsEnabled: Boolean,
    lastHapticFeedbackLog: String,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSelectReplyMessage: (MessageEntity?) -> Unit,
    onSimulateLiveFileTransfer: (MessageEntity) -> Unit,
    onSelectSelfDestructTimer: (Long?) -> Unit,
    onSendStealthFile: (String, String, Long) -> Unit,
    onCapturePrivacyPhoto: (String, Int, Int) -> Unit,
    onScheduleSms: (String, Int, Long) -> Unit,
    onUpdatePeerNickname: (String) -> Unit,
    onToggleSimSlot: () -> Unit,
    onRotateSignalRatchet: () -> Unit,
    onToggleVerifySafetyNumber: () -> Unit,
    onToggleTelemetryExpanded: () -> Unit,
    onCycleFontScale: () -> Unit,
    onSelectFontScale: (FontScalePreset) -> Unit,
    onCycleContrastMode: () -> Unit,
    onSelectContrastMode: (AdaptiveContrastMode) -> Unit,
    onToggleHaptics: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var messageText by remember { mutableStateOf("") }
    var showEmojiTray by remember { mutableStateOf(false) }
    var showAttachBottomSheet by remember { mutableStateOf(false) }
    var showPrivacyCameraDialog by remember { mutableStateOf(false) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }
    var showScheduleSmsDialog by remember { mutableStateOf(false) }
    var showSignalSafetyNumberDialog by remember { mutableStateOf(false) }

    val palette = remember(adaptiveContrastMode) {
        TacticalHapticEngine.resolvePalette(adaptiveContrastMode)
    }
    val scale = fontScalePreset.scaleFactor

    // 1-second local clock tick so self-destructing message countdown badges update live in the UI
    var currentClockMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentClockMillis = System.currentTimeMillis()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val fileName = if (uri != null) {
            "gallery_media_${uri.lastPathSegment?.takeLast(6) ?: "asset"}.jpg"
        } else {
            "encrypted_gallery_snapshot.jpg"
        }
        onSendStealthFile(fileName, "image/jpeg", 3_145_728L)
    }

    val anyFileDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = "custom_drop_${uri.lastPathSegment?.replace(":", "_") ?: "payload.bin"}"
            onSendStealthFile(name, "application/octet-stream", 12_582_912L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundColor)
    ) {
        // Top Header Bar
        Surface(
            color = palette.backgroundColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Conversations",
                            tint = palette.primaryTextColor
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEditNicknameDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = conversation.customNickname,
                                color = palette.primaryTextColor,
                                fontSize = (15.5f * scale).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Custom Nickname",
                                tint = palette.timestampTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (conversation.isBurnerConversation) {
                                "Burner ID (${conversation.peerMeshNodeId}) • Signal E2EE"
                            } else if (conversation.isMeshPeer) {
                                "${conversation.quickShareProfileName} • ${conversation.hopDistance} hops • Signal E2EE"
                            } else {
                                "${conversation.maskedPhoneHint} • Signal E2EE"
                            },
                            color = if (conversation.isBurnerConversation) BleAmber else palette.accentCyanColor,
                            fontSize = (10.5f * scale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Dynamic Font-Size Toggle in Chat Thread
                    Surface(
                        onClick = onCycleFontScale,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("chat_font_scale_button")
                    ) {
                        Text(
                            text = fontScalePreset.shortBadge,
                            color = palette.accentCyanColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        )
                    }

                    // Adaptive Contrast Toggle in Chat Thread
                    Surface(
                        onClick = onCycleContrastMode,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("chat_contrast_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Switch Contrast Mode",
                            tint = BleAmber,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 5.dp)
                                .size(14.dp)
                        )
                    }

                    // Signal Protocol Safety Number & Double Ratchet Verification Button
                    IconButton(
                        onClick = { showSignalSafetyNumberDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("signal_e2ee_verify_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verify Signal Protocol E2EE Safety Number",
                            tint = palette.accentGreenColor
                        )
                    }

                    IconButton(
                        onClick = { showScheduleSmsDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("schedule_sms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScheduleSend,
                            contentDescription = "Schedule SMS via WorkManager",
                            tint = palette.accentCyanColor
                        )
                    }

                    Surface(
                        onClick = onToggleSimSlot,
                        shape = RoundedCornerShape(14.dp),
                        color = palette.inputPillColor,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "SIM $selectedSimSlot",
                            color = OutgoingSamsungTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                // Minimalist Collapsible Contextual Telemetry Bar beneath Chat Header
                if (!conversation.isDecoyRecord) {
                    ContextualTelemetryBar(
                        isExpanded = isTelemetryExpanded,
                        peerCount = peerCount,
                        radioGear = radioGear,
                        isExclusiveOfflineLocked = isExclusiveOfflineLocked,
                        encryptionHealthLabel = if (signalSessionSummary != null) {
                            "Ratchet #${signalSessionSummary.dhRatchetGeneration}"
                        } else {
                            "PQXDH E2EE"
                        },
                        fontScalePreset = fontScalePreset,
                        adaptiveContrastMode = adaptiveContrastMode,
                        palette = palette,
                        isHapticsEnabled = isHapticsEnabled,
                        lastHapticFeedbackLog = lastHapticFeedbackLog,
                        onToggleExpand = onToggleTelemetryExpanded,
                        onSelectFontScale = onSelectFontScale,
                        onSelectContrastMode = onSelectContrastMode,
                        onToggleHaptics = onToggleHaptics
                    )
                }

                // Self-Destructing Message Timer Bar (Off, 5s, 1m, 1h — Wipes from BOTH Sender & Recipient Devices)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.backgroundColor)
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (selectedSelfDestructSeconds != null) BleAmber else palette.timestampTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Self-Destruct • ⇄ Swipe Bubble to Reply:",
                            color = if (selectedSelfDestructSeconds != null) BleAmber else palette.timestampTextColor,
                            fontSize = (10.5f * scale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        selfDestructPresets.forEach { preset ->
                            val isSelected = selectedSelfDestructSeconds == preset.seconds
                            Surface(
                                onClick = { onSelectSelfDestructTimer(preset.seconds) },
                                color = if (isSelected) OutgoingSamsungTeal else palette.inputPillColor,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("self_destruct_chip_${preset.label}")
                            ) {
                                Text(
                                    text = preset.label,
                                    color = if (isSelected) Color.White else palette.secondaryTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = DividerSubtle, thickness = 0.5.dp)
            }
        }

        // Messages List with Swipe-to-Reply & Fluid Progress Rings
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                SwipeToReplyBubbleWrapper(
                    messageId = msg.id,
                    isOutgoing = msg.isOutgoing,
                    accentColor = palette.accentCyanColor,
                    onTriggerReply = { onSelectReplyMessage(msg) }
                ) {
                    SamsungChatBubbleItem(
                        message = msg,
                        expectedPeerId = conversation.id,
                        currentClockMillis = currentClockMillis,
                        fontScale = scale,
                        palette = palette,
                        onQuickReply = { onSelectReplyMessage(msg) },
                        onSimulateLiveFileTransfer = { onSimulateLiveFileTransfer(msg) }
                    )
                }
            }
        }

        // Active Swipe-to-Reply Quote Banner right above Composer Bar
        AnimatedVisibility(visible = replyingToMessage != null) {
            replyingToMessage?.let { quoted ->
                val quotedPlain = remember(quoted.bodyCiphertext, quoted.isDecoyRecord, conversation.id) {
                    if (quoted.isDecoyRecord) quoted.bodyCiphertext
                    else CryptoVaultEngine.decryptAes256Gcm(quoted.bodyCiphertext, conversation.id)
                }
                Surface(
                    color = palette.elevatedSurfaceColor,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .testTag("active_reply_quote_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = palette.accentCyanColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (quoted.isOutgoing) "Replying to Yourself" else "Replying to ${conversation.customNickname}",
                                    color = palette.accentCyanColor,
                                    fontSize = (11f * scale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = quotedPlain,
                                    color = palette.secondaryTextColor,
                                    fontSize = (11.5f * scale).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { onSelectReplyMessage(null) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("cancel_reply_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Reply",
                                tint = palette.timestampTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Optional Unicode Emoji Quick Bar
        AnimatedVisibility(visible = showEmojiTray) {
            val emojis = listOf("👍", "🔥", "🛡️", "📡", "📍", "🚨", "✅", "🔒", "⚡", "🚁", "🩺", "📦")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.elevatedSurfaceColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { messageText += emoji }
                            .padding(4.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = DividerSubtle, thickness = 0.5.dp)

        // SAMSUNG SMS CLONE BOTTOM BAR: 5 EXACT CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.backgroundColor)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Control 1: Gallery
            IconButton(
                onClick = {
                    try {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    } catch (_: Exception) {
                        onSendStealthFile("encrypted_gallery_photo.jpg", "image/jpeg", 2_621_440L)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_control_1_gallery")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = palette.secondaryTextColor
                )
            }

            // Control 2: Privacy Evidence Camera
            IconButton(
                onClick = { showPrivacyCameraDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_control_2_camera")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Privacy Evidence Camera",
                    tint = palette.secondaryTextColor
                )
            }

            // Control 3: Plus (+) Attach (Stealth Mesh-Drop *.*)
            IconButton(
                onClick = { showAttachBottomSheet = true },
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_control_3_attach")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(palette.inputPillColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus Attach Any-Format File",
                        tint = palette.primaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Control 4: Dark Graphite Input Pill (#1C1C1E) with Unicode Emoji Support
            Surface(
                color = palette.inputPillColor,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        textStyle = TextStyle(
                            color = palette.primaryTextColor,
                            fontSize = (15f * scale).sp
                        ),
                        cursorBrush = SolidColor(OutgoingSamsungTeal),
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_control_4_input_pill"),
                        decorationBox = { innerTextField ->
                            if (messageText.isEmpty()) {
                                Text(
                                    text = if (conversation.isMeshPeer) "Signal E2EE message..." else "E2EE SMS (SIM $selectedSimSlot)...",
                                    color = palette.timestampTextColor,
                                    fontSize = (13.5f * scale).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.TagFaces,
                        contentDescription = "Toggle Unicode Emoji Bar",
                        tint = if (showEmojiTray) OutgoingSamsungTeal else palette.timestampTextColor,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showEmojiTray = !showEmojiTray }
                            .testTag("emoji_toggle_icon")
                    )
                }
            }

            // Control 5: Voice Waveform / Send Button
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    } else {
                        onSendMessage("🎙️ [Encrypted Voice Note • 0:08 • Opus 24kbps • Signal Double Ratchet]")
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("bottom_control_5_voice_or_send")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OutgoingSamsungTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (messageText.isNotBlank()) {
                            Icons.AutoMirrored.Filled.Send
                        } else {
                            Icons.Default.GraphicEq
                        },
                        contentDescription = if (messageText.isNotBlank()) "Send Message" else "Voice Waveform Note",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Signal Protocol Safety Number & Double Ratchet Verification Dialog
    if (showSignalSafetyNumberDialog && signalSessionSummary != null) {
        AlertDialog(
            onDismissRequest = { showSignalSafetyNumberDialog = false },
            containerColor = ElevatedCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = WifiDirectGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Signal Protocol E2EE Session", color = PrimaryTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "X3DH (Extended Triple Diffie-Hellman) + Double Ratchet AEAD ensures ONLY you and ${conversation.customNickname} can decrypt messages.",
                        color = SecondaryTextGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = GraphiteInputPill,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("60-DIGIT SIGNAL SAFETY NUMBER", color = MeshCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = signalSessionSummary.safetyNumberFormatted,
                                color = PrimaryTextWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• DH Ratchet Generation: #${signalSessionSummary.dhRatchetGeneration}\n" +
                            "• Send / Recv Chain Index: ${signalSessionSummary.outgoingChainIndex} / ${signalSessionSummary.incomingChainIndex}\n" +
                            "• Ephemeral PubKey: ${signalSessionSummary.localRatchetPubHex}\n" +
                            "• Peer Identity Key: ${signalSessionSummary.peerIdentityFingerprint}",
                        color = TimestampMutedGrey,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRotateSignalRatchet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
                ) {
                    Text("Step DH Ratchet", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onToggleVerifySafetyNumber()
                    showSignalSafetyNumberDialog = false
                }) {
                    Text(if (signalSessionSummary.isVerified) "Done" else "Mark Verified", color = WifiDirectGreen)
                }
            }
        )
    }

    // Modal Bottom Sheet for Control #3: "Stealth Mesh-Drop" Any-Format (*.*) File Transfer
    if (showAttachBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachBottomSheet = false },
            containerColor = ElevatedCardBg,
            contentColor = PrimaryTextWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = MeshCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Stealth Mesh-Drop (*.* Any-Format)",
                            color = PrimaryTextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Signal E2EE + Auto-Gear Shift: BLE 5.0 -> 5GHz Wi-Fi Direct (50–100 MB/s) • 256KB BLAKE3 Chunks",
                            color = TimestampMutedGrey,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                anyFormatPresets.forEach { preset ->
                    Surface(
                        onClick = {
                            showAttachBottomSheet = false
                            onSendStealthFile(preset.fileName, preset.mimeType, preset.sizeBytes)
                        },
                        color = GraphiteInputPill,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    color = PrimaryTextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${preset.fileName} • ${GcMessagesViewModel.formatFileSize(preset.sizeBytes)} • ${((preset.sizeBytes + 262143) / 262144)} BLAKE3 chunks",
                                    color = TimestampMutedGrey,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                color = OutgoingSamsungTeal.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = preset.formatBadge,
                                    color = WifiDirectGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        showAttachBottomSheet = false
                        try {
                            anyFileDocumentLauncher.launch(arrayOf("*/*"))
                        } catch (_: Exception) {
                            onSendStealthFile("tactical_bundle_archive.zip", "application/zip", 8_388_608L)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = MeshCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse System Files (*.* Any Format)", color = MeshCyan)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPrivacyCameraDialog) {
        PrivacyEvidenceCameraDialog(
            onCaptureAndSend = { scene, faces, blurSize ->
                showPrivacyCameraDialog = false
                onCapturePrivacyPhoto(scene, faces, blurSize)
            },
            onDismiss = { showPrivacyCameraDialog = false }
        )
    }

    if (showEditNicknameDialog) {
        var editedName by remember { mutableStateOf(conversation.customNickname) }
        AlertDialog(
            onDismissRequest = { showEditNicknameDialog = false },
            containerColor = ElevatedCardBg,
            title = {
                Text("Identity Privacy Nickname", color = PrimaryTextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Raw phone numbers are strictly hidden in Mesh/Quick Share mode. Set a custom operator nickname for ${conversation.quickShareProfileName}:",
                        color = SecondaryTextGrey,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        label = { Text("Custom Nickname") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePeerNickname(editedName)
                        showEditNicknameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
                ) {
                    Text("Save Nickname", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNicknameDialog = false }) {
                    Text("Cancel", color = TimestampMutedGrey)
                }
            }
        )
    }

    if (showScheduleSmsDialog) {
        var schedText by remember { mutableStateOf("Status check: Rally point Alpha secure.") }
        var delayMins by remember { mutableIntStateOf(15) }
        AlertDialog(
            onDismissRequest = { showScheduleSmsDialog = false },
            containerColor = ElevatedCardBg,
            title = {
                Text("Schedule SMS (WorkManager)", color = PrimaryTextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Dispatch via Dual-SIM Slot $selectedSimSlot with Signal E2EE & Strict Cellular/Mesh Isolation:",
                        color = SecondaryTextGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = schedText,
                        onValueChange = { schedText = it },
                        label = { Text("E2EE Queued Payload") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Delay: $delayMins minutes", color = MeshCyan, fontSize = 12.sp)
                    Slider(
                        value = delayMins.toFloat(),
                        onValueChange = { delayMins = it.toInt().coerceAtLeast(1) },
                        valueRange = 1f..120f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onScheduleSms(schedText, selectedSimSlot, delayMins.toLong())
                        showScheduleSmsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
                ) {
                    Text("Queue in WorkManager", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleSmsDialog = false }) {
                    Text("Cancel", color = TimestampMutedGrey)
                }
            }
        )
    }
}

@Composable
private fun SamsungChatBubbleItem(
    message: MessageEntity,
    expectedPeerId: String,
    currentClockMillis: Long,
    fontScale: Float,
    palette: com.example.service.AdaptiveContrastPalette,
    onQuickReply: () -> Unit,
    onSimulateLiveFileTransfer: () -> Unit
) {
    val decryptedBody = remember(message.bodyCiphertext, message.isDecoyRecord, expectedPeerId) {
        if (message.isDecoyRecord) message.bodyCiphertext
        else CryptoVaultEngine.decryptAes256Gcm(message.bodyCiphertext, expectedPeerId)
    }
    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }
    val remainingSeconds = message.expiresAtTimestamp?.let { exp ->
        ((exp - currentClockMillis) / 1000L).coerceAtLeast(0L)
    }

    val isOutgoing = message.isOutgoing
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isOutgoing) 18.dp else 4.dp,
        bottomEnd = if (isOutgoing) 4.dp else 18.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // For OUTGOING messages: Timestamp + Quick Reply is placed OUTSIDE on the bottom-left in muted grey (#8E8E93)
            if (isOutgoing) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 6.dp, bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply to Message",
                        tint = palette.timestampTextColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onQuickReply() }
                            .testTag("reply_msg_${message.id}")
                    )
                    if (remainingSeconds != null) {
                        Text(
                            text = "🔥 ${remainingSeconds}s",
                            color = BleAmber,
                            fontSize = (10f * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (message.ttlHopsRemaining > 0) {
                        Text(
                            text = "TTL ${message.ttlHopsRemaining}/15",
                            color = palette.accentCyanColor.copy(alpha = 0.85f),
                            fontSize = (9f * fontScale).sp
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = palette.timestampTextColor,
                        fontSize = (11f * fontScale).sp
                    )
                }
            }

            // Bubble Body
            Surface(
                color = if (isOutgoing) palette.outgoingBubbleColor else palette.incomingBubbleColor,
                shape = bubbleShape,
                modifier = Modifier
                    .widthIn(max = 295.dp)
                    .then(
                        if (palette.bubbleBorderWidthDp > 0f) {
                            Modifier.border(
                                width = palette.bubbleBorderWidthDp.dp,
                                color = if (isOutgoing) palette.accentGreenColor else palette.accentCyanColor,
                                shape = bubbleShape
                            )
                        } else Modifier
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Inline Swipe-to-Reply Quoted Block if this message is a reply
                    if (!message.replyToPreviewText.isNullOrBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(WifiDirectGreen)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Column {
                                    Text(
                                        text = message.replyToSenderLabel ?: "Quoted Message",
                                        color = WifiDirectGreen,
                                        fontSize = (10f * fontScale).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = message.replyToPreviewText,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = (10.5f * fontScale).sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Fluid Progress Ring + Inline File Preview Card
                    if (message.attachmentFileName != null) {
                        FluidProgressRingFileCard(
                            message = message,
                            fontScale = fontScale,
                            onSimulateLiveTransfer = onSimulateLiveFileTransfer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = decryptedBody,
                        color = if (isOutgoing) palette.outgoingTextColor else palette.incomingTextColor,
                        fontSize = (14.5f * fontScale).sp,
                        lineHeight = (20f * fontScale).sp
                    )

                    // Signal E2EE Double Ratchet & Self-Destruct badge inside bubble footer
                    if (!message.isDecoyRecord) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = (if (isOutgoing) palette.outgoingTextColor else palette.incomingTextColor).copy(alpha = 0.75f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = buildString {
                                    append("E2EE #${message.e2eeRatchetGeneration}.${message.e2eeMessageNumber}")
                                    if (remainingSeconds != null) {
                                        append(" • 🔥 Self-Destruct ${remainingSeconds}s")
                                    } else {
                                        append(
                                            when (message.transportMode) {
                                                "WIFI_DIRECT_5GHZ" -> " • 5GHz Direct"
                                                "MESH_DTN" -> " • Hop ${message.currentHopCount}/15"
                                                else -> " • SIM ${message.simSlotUsed}"
                                            }
                                        )
                                    }
                                },
                                color = if (remainingSeconds != null) {
                                    BleAmber
                                } else {
                                    (if (isOutgoing) palette.outgoingTextColor else palette.incomingTextColor).copy(alpha = 0.75f)
                                },
                                fontSize = (9.5f * fontScale).sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // For INCOMING messages: Timestamp + Quick Reply is placed OUTSIDE on the bottom-right in muted grey (#8E8E93)
            if (!isOutgoing) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply to Message",
                        tint = palette.timestampTextColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onQuickReply() }
                            .testTag("reply_msg_${message.id}")
                    )
                    if (remainingSeconds != null) {
                        Text(
                            text = "🔥 ${remainingSeconds}s",
                            color = BleAmber,
                            fontSize = (10f * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (message.ttlHopsRemaining > 0) {
                        Text(
                            text = "Hop ${message.currentHopCount}",
                            color = palette.accentCyanColor.copy(alpha = 0.85f),
                            fontSize = (9f * fontScale).sp
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = palette.timestampTextColor,
                        fontSize = (11f * fontScale).sp
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyEvidenceCameraDialog(
    onCaptureAndSend: (sceneTitle: String, facesCount: Int, blurStrength: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sceneTitle by remember { mutableStateOf("Sector-4 Checkpoint Recon") }
    var facesToBlur by remember { mutableIntStateOf(2) }
    var blurStrength by remember { mutableFloatStateOf(18f) }

    val previewCapture = remember(sceneTitle, facesToBlur, blurStrength) {
        CryptoVaultEngine.generatePrivacyEvidencePhoto(
            sceneTitle = sceneTitle,
            facesDetected = facesToBlur,
            blurStrength = blurStrength.toInt()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = MeshCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacy Evidence Camera",
                    color = PrimaryTextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Automatically blurs human faces, strips EXIF/GPS metadata, and encrypts via Signal Double Ratchet before 256KB BLAKE3 mesh transmission.",
                    color = SecondaryTextGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    bitmap = previewCapture.bitmap.asImageBitmap(),
                    contentDescription = "Redacted Privacy Evidence Frame",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MeshCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Faces Detected & Blurred: $facesToBlur", color = WifiDirectGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Pixel Block: ${blurStrength.toInt()}px", color = MeshCyan, fontSize = 12.sp)
                }
                Slider(
                    value = blurStrength,
                    onValueChange = { blurStrength = it },
                    valueRange = 10f..30f
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { count ->
                        OutlinedButton(
                            onClick = { facesToBlur = count },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$count Face${if (count > 1) "s" else ""}", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Stripped Tags: ${previewCapture.strippedExifTags.joinToString(", ")}",
                    color = TimestampMutedGrey,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCaptureAndSend(sceneTitle, facesToBlur, blurStrength.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = OutgoingSamsungTeal)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scrub & Mesh-Drop", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TimestampMutedGrey)
            }
        }
    )
}

@Composable
private fun ContactsContractPickerDialog(
    contacts: List<LocalContactEntry>,
    onSelectContact: (LocalContactEntry) -> Unit,
    onOpenBurnerScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedCardBg,
        title = {
            Text(
                text = "Start E2EE Conversation",
                color = PrimaryTextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Button(
                    onClick = onOpenBurnerScreen,
                    colors = ButtonDefaults.buttonColors(containerColor = BleAmber.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = BleAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Isolated Burner Contacts", color = BleAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Device ContactsContract (Identity Masked):",
                    color = SecondaryTextGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                contacts.take(5).forEach { contact ->
                    Surface(
                        onClick = { onSelectContact(contact) },
                        color = GraphiteInputPill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(contact.displayName, color = PrimaryTextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(contact.maskedIdentityLabel, color = MeshCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MeshCyan)
            }
        }
    )
}
