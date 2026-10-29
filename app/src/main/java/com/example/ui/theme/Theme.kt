package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GcMessagesAmoledColorScheme = darkColorScheme(
    primary = OutgoingSamsungTeal,
    onPrimary = Color.White,
    primaryContainer = OutgoingSamsungTeal,
    onPrimaryContainer = Color.White,
    secondary = MeshCyan,
    onSecondary = AmoledBlack,
    secondaryContainer = ElevatedCardBg,
    onSecondaryContainer = PrimaryTextWhite,
    tertiary = WifiDirectGreen,
    background = AmoledBlack,
    onBackground = PrimaryTextWhite,
    surface = AmoledBlack,
    onSurface = PrimaryTextWhite,
    surfaceVariant = GraphiteInputPill,
    onSurfaceVariant = TimestampMutedGrey,
    error = DuressRed,
    outline = DividerSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GcMessagesAmoledColorScheme,
        typography = Typography,
        content = content
    )
}
