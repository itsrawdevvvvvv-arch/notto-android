package com.notto.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notto.app.data.NotifConfig
import com.notto.app.ui.components.pressable
import com.notto.app.ui.theme.LocalNottoColors
import com.notto.app.ui.theme.NottoMotion
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

@Composable
fun FocusScreen() {
    val colors = LocalNottoColors.current
    val haptic = LocalHapticFeedback.current
    val activities = remember { listOf("Write", "Plan", "Sketch", "Read") }
    var actIdx by remember { mutableIntStateOf(0) }
    var focusMins by remember { mutableIntStateOf(25) }
    var remainingSec by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }

    var holding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(running) {
        while (running && remainingSec > 0) {
            delay(1000)
            remainingSec -= 1
            if (remainingSec == 0) {
                running = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    LaunchedEffect(holding) {
        if (holding) {
            val start = System.currentTimeMillis()
            while (holding) {
                val p = ((System.currentTimeMillis() - start) / 650f).coerceIn(0f, 1f)
                holdProgress = p
                if (p >= 1f) {
                    holding = false
                    holdProgress = 0f
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (running) {
                        running = false
                        remainingSec = focusMins * 60
                    } else {
                        if (remainingSec == 0) remainingSec = focusMins * 60
                        running = true
                    }
                    break
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "focus_head") {
            Column {
                Text("DAILY RITUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted, letterSpacing = 1.1.sp)
                Spacer(Modifier.height(4.dp))
                Text("Focus commit.", fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp, color = colors.text)
            }
        }

        item(key = "goal") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("My goal is to", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.accent.copy(alpha = 0.16f))
                            .pressable { actIdx = (actIdx + 1) % activities.size }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(activities[actIdx], fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                    }
                    Text("for $focusMins min", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                }
            }
        }

        item(key = "ruler") {
            val barCount = 25
            val activeIdx = ((focusMins - 5f) / 2.5f).roundToInt().coerceIn(0, barCount - 1)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF16161B))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DRAG RULER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.55f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (running) colors.green else colors.accent)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (running) "RUNNING" else "READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(10.dp))
                val mins = remainingSec / 60
                val secs = remainingSec % 60
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(10.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .pointerInput(running) {
                            if (running) return@pointerInput
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                                val idx = (ratio * (barCount - 1)).roundToInt()
                                val newMins = (5 + idx * 2.5f).roundToInt()
                                if (newMins != focusMins) {
                                    focusMins = newMins
                                    remainingSec = newMins * 60
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                ) {
                    val slotW = size.width / barCount
                    val barW = (slotW * 0.52f).coerceAtLeast(3.dp.toPx())
                    for (i in 0 until barCount) {
                        val dist = abs(i - activeIdx).toFloat()
                        val hFrac = (0.25f + 0.75f * exp(-(dist * dist) / 7f))
                        val barH = size.height * hFrac
                        val c = when {
                            dist == 0f -> Color(0xFF7A78FF)
                            dist <= 2f -> Color(0xFF7A78FF).copy(alpha = 0.55f)
                            else -> Color.White.copy(alpha = 0.25f)
                        }
                        drawRoundRect(
                            color = c,
                            topLeft = Offset(i * slotW + (slotW - barW) / 2f, size.height - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 25, 45, 60).forEach { m ->
                        val sel = focusMins == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (sel) colors.accent else Color.White.copy(alpha = 0.12f))
                                .pressable {
                                    if (!running) {
                                        focusMins = m
                                        remainingSec = m * 60
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("${m}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item(key = "commit_btn") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    holding = true
                                    tryAwaitRelease()
                                    holding = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = colors.hairline,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        if (holdProgress > 0f) {
                            drawArc(
                                color = colors.accent,
                                startAngle = -90f,
                                sweepAngle = 360f * holdProgress,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(if (running) colors.red else colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (running) Icons.Rounded.Close else Icons.Rounded.Check,
                            contentDescription = "Hold to commit",
                            tint = if (running) Color.White else colors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (running) "HOLD TO STOP" else "HOLD TO COMMIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    name: String,
    avatarEmoji: String,
    darkTheme: Boolean,
    sortAZ: Boolean,
    notif: NotifConfig,
    onToggleTheme: (Boolean) -> Unit,
    onToggleSort: (Boolean) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotif: () -> Unit,
    onResetDemo: () -> Unit
) {
    val colors = LocalNottoColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "set_head") {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("PREFERENCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted, letterSpacing = 1.1.sp)
                Spacer(Modifier.height(4.dp))
                Text("Settings", fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp, color = colors.text)
            }
        }

        item(key = "prof") {
            SettingPillRow(
                leadingEmoji = avatarEmoji,
                title = "Profile",
                value = name,
                onClick = onOpenProfile
            )
        }

        item(key = "notif") {
            SettingPillRow(
                leadingEmoji = "🔔",
                title = "Custom Notification",
                value = if (notif.enabled) "%02d:%02d".format(notif.hour, notif.minute) else "Off",
                onClick = onOpenNotif
            )
        }

        item(key = "theme") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🌙", fontSize = 18.sp)
                    Text("Dark Theme", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                }
                Switch(
                    checked = darkTheme,
                    onCheckedChange = onToggleTheme,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.accent)
                )
            }
        }

        item(key = "sort") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⇅", fontSize = 18.sp)
                    Text("Sort A–Z", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                }
                Switch(
                    checked = sortAZ,
                    onCheckedChange = onToggleSort,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.accent)
                )
            }
        }

        item(key = "reset") {
            SettingPillRow(
                leadingEmoji = "↺",
                title = "Reset Demo Notes",
                value = "Local",
                onClick = onResetDemo
            )
        }
    }
}

@Composable
private fun SettingPillRow(
    leadingEmoji: String,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val colors = LocalNottoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .pressable(onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(leadingEmoji, fontSize = 19.sp)
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, fontSize = 14.sp, color = colors.muted)
            Text("›", fontSize = 20.sp, color = colors.muted, fontWeight = FontWeight.Bold)
        }
    }
}
