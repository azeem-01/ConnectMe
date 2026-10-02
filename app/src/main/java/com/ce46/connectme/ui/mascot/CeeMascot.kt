package com.ce46.connectme.ui.mascot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ce46.connectme.data.ConnectionState

/**
 * Custom speech bubble shape with rounded corners and a downward-pointing tail.
 */
class SpeechBubbleShape(
    val cornerRadius: Dp = 16.dp,
    val tailWidth: Dp = 14.dp,
    val tailHeight: Dp = 10.dp,
    val tailOffsetPercent: Float = 0.5f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Generic(Path())
        }

        val tailHPx = with(density) { tailHeight.toPx() }
        val tailWPx = with(density) { tailWidth.toPx() }
        val rawRPx = with(density) { cornerRadius.toPx() }

        val bubbleH = (size.height - tailHPx).coerceAtLeast(0f)
        val maxR = minOf(size.width / 2f, bubbleH / 2f)
        val r = rawRPx.coerceIn(0f, maxR.coerceAtLeast(0f))

        val tailCenterX = size.width * tailOffsetPercent.coerceIn(0f, 1f)
        val tailLeft = (tailCenterX - tailWPx / 2f).coerceIn(r, (size.width - r - tailWPx).coerceAtLeast(r))
        val tailRight = (tailLeft + tailWPx).coerceAtMost((size.width - r).coerceAtLeast(tailLeft))
        val tailTipX = (tailLeft + tailRight) / 2f
        val tailTipY = size.height

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(size.width - r, 0f)
            if (r > 0f) quadraticBezierTo(size.width, 0f, size.width, r)
            lineTo(size.width, bubbleH - r)
            if (r > 0f) quadraticBezierTo(size.width, bubbleH, size.width - r, bubbleH)
            lineTo(tailRight, bubbleH)
            lineTo(tailTipX, tailTipY)
            lineTo(tailLeft, bubbleH)
            lineTo(r, bubbleH)
            if (r > 0f) quadraticBezierTo(0f, bubbleH, 0f, bubbleH - r)
            lineTo(0f, r)
            if (r > 0f) quadraticBezierTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Returns dynamic campus mascot dialogue based on connection state and credentials.
 */
fun getCeeDialogue(
    state: ConnectionState,
    hasCredentials: Boolean,
    autoLoginEnabled: Boolean
): String = when {
    !hasCredentials -> "Pehle ID aur password daalo janaab, phir Cee sambhal lega! 🎓"
    !autoLoginEnabled -> "Cee is on chai break ☕ Flip auto-login on when you're ready!"
    state == ConnectionState.LOGGING_IN -> "Fortinet portal se handshake ho raha hai... ⚡"
    state == ConnectionState.LOGGED_IN -> "Done ho gaya! Campus internet is ready ✅"
    state == ConnectionState.ERROR -> "Portal ne thoda tang kiya, retrying in a sec... 🔄"
    state == ConnectionState.WATCHING -> "Cee on duty! Campus Wi-Fi dhoond raha hoon 📡"
    else -> "Ready for campus Wi-Fi! 🫡"
}

/**
 * Comic speech bubble with animated entry/exit.
 */
@Composable
fun CeeSpeechBubble(
    dialogue: String,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = dialogue,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 50)) +
                scaleIn(
                    initialScale = 0.88f,
                    transformOrigin = TransformOrigin(0.5f, 1.0f),
                    animationSpec = tween(220, delayMillis = 50, easing = FastOutSlowInEasing)
                ))
                .togetherWith(
                    fadeOut(animationSpec = tween(140)) +
                        scaleOut(
                            targetScale = 0.88f,
                            transformOrigin = TransformOrigin(0.5f, 1.0f),
                            animationSpec = tween(140, easing = FastOutSlowInEasing)
                        )
                )
        },
        label = "CeeSpeechBubbleAnimation",
        modifier = modifier
    ) { currentText ->
        if (currentText.isNotBlank()) {
            val bubbleShape = remember { SpeechBubbleShape() }
            Surface(
                shape = bubbleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .widthIn(min = 140.dp, max = 280.dp)
                    .wrapContentSize()
            ) {
                Box(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 18.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Vector mascot Cee with Canvas rendering, bobbing, pulsing, and facial animations.
 */
@Composable
fun CeeMascot(
    state: ConnectionState,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    val ring = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val face = MaterialTheme.colorScheme.onPrimary

    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobAnim"
    )

    val pulseSpeed = when (state) {
        ConnectionState.LOGGING_IN -> 450
        ConnectionState.ERROR -> 700
        else -> 1700
    }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Canvas(modifier = modifier.size(136.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f + bob)
        val r = size.minDimension * 0.32f * pulse

        // Main body
        drawCircle(color = color, radius = r, center = c)

        // Eyes & expression
        val eyeY = c.y - r * 0.12f
        if (state == ConnectionState.LOGGED_IN) {
            // Happy happy squinty eyes (^ ^)
            val eyeR = r * 0.14f
            drawArc(
                color = face,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(c.x - r * 0.28f, eyeY - eyeR * 0.5f),
                size = Size(eyeR * 1.3f, eyeR),
                style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
            )
            drawArc(
                color = face,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(c.x + r * 0.12f, eyeY - eyeR * 0.5f),
                size = Size(eyeR * 1.3f, eyeR),
                style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
            )
        } else {
            // Normal rounded eyes
            val eyeD = r * 0.11f
            drawCircle(face, eyeD, Offset(c.x - r * 0.22f, eyeY))
            drawCircle(face, eyeD, Offset(c.x + r * 0.22f, eyeY))
        }

        // Cute smile
        drawArc(
            color = face,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(c.x - r * 0.26f, c.y + r * 0.02f),
            size = Size(r * 0.52f, r * 0.38f),
            style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
        )

        // Pulsing Wi-Fi antenna rings above Cee
        val arcStroke = Stroke(width = r * 0.11f, cap = StrokeCap.Round)
        drawArc(
            color = ring,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(c.x - r * 1.35f, c.y - r * 1.55f),
            size = Size(r * 2.7f, r * 2.2f),
            style = arcStroke
        )
        drawArc(
            color = ring.copy(alpha = 0.55f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(c.x - r * 1.7f, c.y - r * 2.0f),
            size = Size(r * 3.4f, r * 2.8f),
            style = arcStroke
        )
    }
}

/**
 * Combined composable rendering Cee with reactive dialogue speech bubble above.
 */
@Composable
fun AnimatedCeeWithDialogue(
    state: ConnectionState,
    hasCredentials: Boolean,
    autoLoginEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dialogue = remember(state, hasCredentials, autoLoginEnabled) {
        getCeeDialogue(state, hasCredentials, autoLoginEnabled)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        CeeSpeechBubble(
            dialogue = dialogue,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        CeeMascot(state = state)
    }
}
