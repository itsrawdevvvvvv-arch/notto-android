package com.notto.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notto.app.data.CheckItem
import com.notto.app.data.NoteItem
import com.notto.app.data.NotifConfig
import com.notto.app.ui.components.AnimatedCheckRow
import com.notto.app.ui.components.pressable
import com.notto.app.ui.theme.LocalNottoColors
import com.notto.app.ui.theme.NottoMotion
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Ember(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val r: Float,
    val color: Color
)

@Composable
fun EditorSheet(
    note: NoteItem?,
    onDismissAndSave: (NoteItem) -> Unit,
    onDelete: (String) -> Unit
) {
    AnimatedVisibility(
        visible = note != null,
        enter = fadeIn(NottoMotion.standardTween(200)) + slideInVertically(NottoMotion.softSpring()) { it },
        exit = fadeOut(NottoMotion.standardTween(180)) + slideOutVertically(NottoMotion.standardTween(240)) { it }
    ) {
        if (note == null) return@AnimatedVisibility
        val colors = LocalNottoColors.current
        val haptic = LocalHapticFeedback.current

        var title by remember(note.id) { mutableStateOf(note.title) }
        var body by remember(note.id) { mutableStateOf(note.body) }
        var pinned by remember(note.id) { mutableStateOf(note.pinned) }
        var textSize by remember(note.id) { mutableFloatStateOf(note.textSizeSp) }
        val items = remember(note.id) { mutableStateListOf<CheckItem>().apply { addAll(note.items) } }
        var newItemText by remember { mutableStateOf("") }

        // Hold-to-burn state
        var holdingBurn by remember { mutableStateOf(false) }
        var burnCharge by remember { mutableFloatStateOf(0f) }
        var burning by remember { mutableStateOf(false) }
        var burnProgress by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(holdingBurn) {
            if (holdingBurn) {
                val t0 = System.currentTimeMillis()
                while (holdingBurn) {
                    val p = ((System.currentTimeMillis() - t0) / 700f).coerceIn(0f, 1f)
                    burnCharge = p
                    if (p >= 1f) {
                        holdingBurn = false
                        burnCharge = 0f
                        burning = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        break
                    }
                    delay(16)
                }
            } else {
                burnCharge = 0f
            }
        }

        LaunchedEffect(burning) {
            if (burning) {
                val t0 = System.currentTimeMillis()
                while (true) {
                    val p = ((System.currentTimeMillis() - t0) / 650f).coerceIn(0f, 1f)
                    burnProgress = p
                    if (p >= 1f) {
                        onDelete(note.id)
                        break
                    }
                    delay(16)
                }
            }
        }

        val currentUpdated = {
            note.copy(
                title = title.trim().ifEmpty { "Untitled" },
                body = body,
                pinned = pinned,
                textSizeSp = textSize,
                items = items.toList(),
                dateLabel = "Today",
                updatedAt = System.currentTimeMillis()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(colors.surface)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.bg)
                            .pressable { onDismissAndSave(currentUpdated()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (pinned) colors.accent else colors.bg)
                                .pressable { pinned = !pinned },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Star, contentDescription = "Pin", tint = if (pinned) Color.White else colors.muted)
                        }

                        // Hold-to-burn button
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.red)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            holdingBurn = true
                                            tryAwaitRelease()
                                            holdingBurn = false
                                        }
                                    )
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (burnCharge > 0f) "Burning ${(burnCharge * 100).toInt()}%" else "Hold to Burn",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Content + Burn canvas
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .graphicsLayer {
                                alpha = 1f - burnProgress
                                translationY = -burnProgress * 32f
                            }
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                color = colors.text,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.6).sp
                            ),
                            cursorBrush = SolidColor(colors.accent),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("${note.dateLabel} • ${if (note.type == "checklist") "${items.count { it.done }}/${items.size} done" else "${body.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size} words"}", fontSize = 12.sp, color = colors.muted)
                        Spacer(Modifier.height(14.dp))

                        if (note.type == "checklist") {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(items, key = { it.id }) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colors.bg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            AnimatedCheckRow(
                                                text = item.text,
                                                done = item.done,
                                                onToggle = {
                                                    val idx = items.indexOfFirst { it.id == item.id }
                                                    if (idx >= 0) items[idx] = items[idx].copy(done = !items[idx].done)
                                                }
                                            )
                                        }
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Remove",
                                            tint = colors.muted,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .pressable { items.removeAll { it.id == item.id } }
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(colors.bg)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (newItemText.isEmpty()) Text("Add task...", color = colors.muted, fontSize = 14.sp)
                                    BasicTextField(
                                        value = newItemText,
                                        onValueChange = { newItemText = it },
                                        singleLine = true,
                                        textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                                        cursorBrush = SolidColor(colors.accent),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(colors.accent)
                                        .pressable {
                                            val t = newItemText.trim()
                                            if (t.isNotEmpty()) {
                                                items.add(CheckItem("c_${System.currentTimeMillis()}", t, false))
                                                newItemText = ""
                                            }
                                        }
                                        .padding(horizontal = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            BasicTextField(
                                value = body,
                                onValueChange = { body = it },
                                textStyle = TextStyle(
                                    color = colors.text,
                                    fontSize = textSize.sp,
                                    lineHeight = (textSize * 1.5f).sp
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }

                    if (burning) {
                        val embers = remember {
                            List(48) {
                                Ember(
                                    x = Random.nextFloat(),
                                    y = 0.7f + Random.nextFloat() * 0.3f,
                                    vx = (Random.nextFloat() - 0.5f) * 0.004f,
                                    vy = -(0.01f + Random.nextFloat() * 0.02f),
                                    r = 5f + Random.nextFloat() * 10f,
                                    color = if (Random.nextBoolean()) Color(0xFFFF9500) else Color(0xFFFF3B30)
                                )
                            }
                        }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            embers.forEach { e ->
                                val px = (e.x + e.vx * burnProgress * 60f) * size.width
                                val py = (e.y + e.vy * burnProgress * 60f) * size.height
                                val alpha = (1f - burnProgress).coerceIn(0f, 1f)
                                drawCircle(
                                    color = e.color.copy(alpha = alpha),
                                    radius = e.r * alpha,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }

                if (note.type != "checklist") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ABC" to { body = body.uppercase() }, "abc" to { body = body.lowercase() }).forEach { (label, act) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.bg)
                                    .pressable(act)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text)
                            }
                        }
                        Slider(
                            value = textSize,
                            onValueChange = { textSize = it },
                            valueRange = 13f..24f,
                            colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(colors.accent)
                        .pressable { onDismissAndSave(currentUpdated()) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save & Close", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CreateNoteSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreate: (type: String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(NottoMotion.standardTween(180)) + slideInVertically(NottoMotion.softSpring()) { it },
        exit = fadeOut(NottoMotion.standardTween(150)) + slideOutVertically(NottoMotion.standardTween(200)) { it }
    ) {
        val colors = LocalNottoColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(colors.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("New Entry", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.bg)
                        .pressable { onCreate("note") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("✎", fontSize = 22.sp)
                    Column {
                        Text("Text Note", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text("Clean sheet with size & case controls", fontSize = 12.sp, color = colors.muted)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.bg)
                        .pressable { onCreate("checklist") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("✓", fontSize = 22.sp, color = colors.accent, fontWeight = FontWeight.Bold)
                    Column {
                        Text("Checklist", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text("Tickable items with animated strikethrough", fontSize = 12.sp, color = colors.muted)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditSheet(
    visible: Boolean,
    currentName: String,
    currentEmoji: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(NottoMotion.softSpring()) { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        val colors = LocalNottoColors.current
        var name by remember(visible) { mutableStateOf(currentName) }
        var emoji by remember(visible) { mutableStateOf(currentEmoji) }
        val emojis = remember { listOf("🐣", "🦊", "🐸", "🦄", "😎", "🐼") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(remember { MutableInteractionSource() }, null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(colors.surface)
                    .clickable(remember { MutableInteractionSource() }, null) {}
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colors.muted, modifier = Modifier.pressable(onDismiss))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    emojis.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (e == emoji) colors.accent.copy(alpha = 0.2f) else colors.bg)
                                .pressable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e, fontSize = 22.sp) }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 24) name = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(colors.accent)
                        .pressable { onSave(name.trim().ifEmpty { "Friend" }, emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Profile", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CustomNotificationSheet(
    visible: Boolean,
    config: NotifConfig,
    onDismiss: () -> Unit,
    onTestNow: (title: String, msg: String) -> Unit,
    onSave: (NotifConfig) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(NottoMotion.softSpring()) { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        val colors = LocalNottoColors.current
        var hour by remember(visible) { mutableIntStateOf(config.hour) }
        var minute by remember(visible) { mutableIntStateOf(config.minute) }
        var title by remember(visible) { mutableStateOf(config.title) }
        var message by remember(visible) { mutableStateOf(config.message) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(remember { MutableInteractionSource() }, null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(colors.surface)
                    .clickable(remember { MutableInteractionSource() }, null) {}
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Custom Notification", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colors.muted, modifier = Modifier.pressable(onDismiss))
                }

                // Interactive Hour/Minute stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.bg)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▲", modifier = Modifier.pressable { hour = (hour + 1) % 24 }.padding(6.dp), color = colors.accent)
                        Text("%02d".format(hour), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text("▼", modifier = Modifier.pressable { hour = (hour + 23) % 24 }.padding(6.dp), color = colors.accent)
                    }
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.muted, modifier = Modifier.padding(horizontal = 16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▲", modifier = Modifier.pressable { minute = (minute + 5) % 60 }.padding(6.dp), color = colors.accent)
                        Text("%02d".format(minute), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text("▼", modifier = Modifier.pressable { minute = (minute + 55) % 60 }.padding(6.dp), color = colors.accent)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(colors.bg)
                            .pressable { onTestNow(title.ifBlank { "Notto" }, message.ifBlank { "Time to jot!" }) }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Test Now", color = colors.text, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(colors.accent)
                            .pressable {
                                onSave(NotifConfig(enabled = true, hour = hour, minute = minute, title = title, message = message))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Enable Daily", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
