package com.notto.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notto.app.ui.components.pressable
import com.notto.app.ui.theme.LocalNottoColors
import kotlinx.coroutines.delay

@Composable
fun MascotLoaderOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(1300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(tween(280))
    ) {
        val colors = LocalNottoColors.current
        val inf = rememberInfiniteTransition(label = "mascot")
        val bounceY by inf.animateFloat(
            initialValue = 4f,
            targetValue = -16f,
            animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bounceY"
        )
        val tilt by inf.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "tilt"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(
                    modifier = Modifier
                        .size(136.dp)
                        .graphicsLayer {
                            translationY = bounceY.dp.toPx()
                            rotationZ = tilt
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    // Mascot body
                    drawRoundRect(
                        color = Color(0xFF5E5CE6),
                        topLeft = Offset(w * 0.18f, h * 0.14f),
                        size = Size(w * 0.64f, h * 0.68f),
                        cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                    )
                    // Folded corner accent
                    val fold = Path().apply {
                        moveTo(w * 0.64f, h * 0.14f)
                        lineTo(w * 0.82f, h * 0.32f)
                        lineTo(w * 0.64f, h * 0.32f)
                        close()
                    }
                    drawPath(fold, color = Color(0xFF9C9AFF))
                    // Eyes
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.38f, h * 0.44f))
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.62f, h * 0.44f))
                    drawCircle(Color(0xFF1C1C1E), radius = 3.dp.toPx(), center = Offset(w * 0.38f, h * 0.44f))
                    drawCircle(Color(0xFF1C1C1E), radius = 3.dp.toPx(), center = Offset(w * 0.62f, h * 0.44f))
                    // Smile
                    drawArc(
                        color = Color.White,
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(w * 0.40f, h * 0.50f),
                        size = Size(w * 0.20f, h * 0.14f),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Notto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Tap anywhere to skip",
                    fontSize = 12.sp,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
fun OnboardingOverlay(
    visible: Boolean,
    initialName: String,
    initialEmoji: String,
    onComplete: (name: String, emoji: String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 4 },
        exit = fadeOut(tween(240))
    ) {
        val colors = LocalNottoColors.current
        val emojis = remember { listOf("🐣", "🦊", "🐸", "🦄", "😎", "🐼") }
        var selectedEmoji by remember { mutableStateOf(initialEmoji) }
        var name by remember { mutableStateOf(if (initialName == "Friend") "" else initialName) }

        val inf = rememberInfiniteTransition(label = "onbAv")
        val floatY by inf.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "floatY"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .graphicsLayer { translationY = floatY.dp.toPx() }
                        .clip(CircleShape)
                        .background(colors.cardGradients[0]),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedEmoji, fontSize = 56.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(colors.surface)
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Make Notto yours",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp,
                    color = colors.text
                )
                Text(
                    text = "Pick an avatar and tell us what to call you.",
                    fontSize = 14.sp,
                    color = colors.muted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.forEach { emoji ->
                        val active = emoji == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (active) colors.accent.copy(alpha = 0.18f) else colors.bg)
                                .pressable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.bg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (name.isEmpty()) {
                        Text("Your name (e.g. Alex)", color = colors.muted, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 24) name = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(colors.accent)
                        .pressable {
                            onComplete(name.trim().ifEmpty { "Friend" }, selectedEmoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Get started", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
