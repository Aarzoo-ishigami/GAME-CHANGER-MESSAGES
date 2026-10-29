package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders the GC Messages App Logo from `assets/1000086219.png`
 * (Blue gradient squircle with white speech bubble, 2 signal arcs, and 'X' mark)
 * accompanied by a bespoke procedural vector fallback painter.
 */
@Composable
fun GcMessagesAppLogo(
    size: Dp = 42.dp,
    forceProceduralVector: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetBitmap = remember(context, forceProceduralVector) {
        if (forceProceduralVector) {
            null
        } else {
            try {
                context.assets.open("1000086219.png").use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.24f)),
        contentAlignment = Alignment.Center
    ) {
        if (assetBitmap != null) {
            Image(
                bitmap = assetBitmap,
                contentDescription = "GC Messages Stealth Mesh Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ProceduralGcMessagesLogoCanvas(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Procedural Vector Fallback Painter:
 * Blue gradient squircle (#00C6FF -> #0052D4) with white speech bubble,
 * 2 concentric signal arcs, and an offline/stealth 'X' mark.
 */
@Composable
fun ProceduralGcMessagesLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color.Transparent)) {
        val w = size.width
        val h = size.height

        // 1. Blue gradient squircle background
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF00C6FF), Color(0xFF0052D4))
            ),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.24f, h * 0.24f)
        )

        // 2. White speech bubble with bottom-left tail
        val bubbleCenter = Offset(w * 0.50f, h * 0.47f)
        val bubbleRadius = w * 0.31f
        drawCircle(
            color = Color.White,
            radius = bubbleRadius,
            center = bubbleCenter
        )

        val tailPath = Path().apply {
            moveTo(w * 0.32f, h * 0.62f)
            lineTo(w * 0.32f, h * 0.78f)
            lineTo(w * 0.47f, h * 0.69f)
            close()
        }
        drawPath(path = tailPath, color = Color.White)

        val iconBlue = Color(0xFF0062E6)
        val strokeW = w * 0.048f

        // 3. Outer & Inner Signal Arcs inside the speech bubble
        val outerArcRect = Rect(
            left = w * 0.31f,
            top = h * 0.28f,
            right = w * 0.69f,
            bottom = h * 0.64f
        )
        drawArc(
            color = iconBlue,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = outerArcRect.topLeft,
            size = outerArcRect.size,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        val innerArcRect = Rect(
            left = w * 0.37f,
            top = h * 0.35f,
            right = w * 0.63f,
            bottom = h * 0.59f
        )
        drawArc(
            color = iconBlue,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = innerArcRect.topLeft,
            size = innerArcRect.size,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // 4. Bold 'X' mark beneath the signal arcs
        val xCenter = Offset(w * 0.50f, h * 0.54f)
        val xHalf = w * 0.055f
        drawLine(
            color = iconBlue,
            start = Offset(xCenter.x - xHalf, xCenter.y - xHalf),
            end = Offset(xCenter.x + xHalf, xCenter.y + xHalf),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = iconBlue,
            start = Offset(xCenter.x + xHalf, xCenter.y - xHalf),
            end = Offset(xCenter.x - xHalf, xCenter.y + xHalf),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}
