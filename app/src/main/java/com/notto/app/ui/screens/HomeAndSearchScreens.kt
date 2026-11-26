package com.notto.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notto.app.data.NoteItem
import com.notto.app.ui.components.AnimatedCheckRow
import com.notto.app.ui.components.pressable
import com.notto.app.ui.theme.LocalNottoColors
import com.notto.app.ui.theme.NottoMotion
import java.util.Calendar
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    name: String,
    avatarEmoji: String,
    notes: List<NoteItem>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNote: (NoteItem) -> Unit,
    onToggleCheck: (noteId: String, itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNottoColors.current
    val greeting = remember(name) {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val prefix = when {
            h < 12 -> "Good morning"
            h < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        "$prefix, $name"
    }

    val filtered = remember(notes, selectedFilter) {
        when (selectedFilter) {
            "Pinned" -> notes.filter { it.pinned }
            "To-do" -> notes.filter { it.type == "checklist" }
            "Notes" -> notes.filter { it.type == "note" }
            "Visual" -> notes.filter { it.type == "visual" }
            else -> notes
        }
    }

    val featured = remember(notes) { notes.take(5) }
    val filters = remember { listOf("All", "Pinned", "To-do", "Notes", "Visual") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 112.dp)
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.pressable(onOpenProfile),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x22000000))
                            .clip(CircleShape)
                            .background(colors.cardGradients[0]),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = greeting,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${notes.size} notes stored locally",
                            fontSize = 12.sp,
                            color = colors.muted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x18000000))
                        .clip(CircleShape)
                        .background(colors.surface)
                        .pressable(onOpenSearch),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search", tint = colors.accent)
                }
            }
        }

        item(key = "hero") {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("WORKSPACE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted, letterSpacing = 1.1.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Everything you\nwrote down.",
                    fontSize = 32.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp,
                    color = colors.text
                )
            }
        }

        item(key = "chips") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { f ->
                    val active = f == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (active) colors.text else colors.surface)
                            .pressable { onSelectFilter(f) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = f,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (active) colors.surface else colors.text
                        )
                    }
                }
            }
        }

        if (selectedFilter == "All" && featured.isNotEmpty()) {
            item(key = "carousel") {
                Carousel3D(featured = featured, onOpenNote = onOpenNote)
            }
        }

        item(key = "sec_title") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("All Notes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text("${filtered.size} items", fontSize = 12.sp, color = colors.muted)
            }
        }

        itemsIndexed(
            items = filtered,
            key = { _, item -> item.id },
            contentType = { _, item -> item.type }
        ) { index, note ->
            StackedNoteBand(
                note = note,
                index = index,
                onClick = { onOpenNote(note) },
                onToggleCheck = { itemId -> onToggleCheck(note.id, itemId) }
            )
        }
    }
}

@Composable
private fun Carousel3D(
    featured: List<NoteItem>,
    onOpenNote: (NoteItem) -> Unit
) {
    val colors = LocalNottoColors.current
    val pagerState = rememberPagerState(pageCount = { featured.size })

    Column(modifier = Modifier.padding(top = 10.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 64.dp),
            pageSpacing = 10.dp
        ) { page ->
            val note = featured[page]
            val grad = colors.cardGradients[note.toneIndex % colors.cardGradients.size]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1.5f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
                    .graphicsLayer {
                        val s = 1f - absOffset * 0.12f
                        scaleX = s
                        scaleY = s
                        rotationY = (-pageOffset * 12f).coerceIn(-15f, 15f)
                        alpha = (1f - absOffset * 0.28f).coerceAtLeast(0.55f)
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(grad)
                    .pressable { onOpenNote(note) }
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surface.copy(alpha = 0.78f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (note.pinned) "★ Pinned" else note.dateLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent
                            )
                        }
                        Text(note.dateLabel, fontSize = 11.sp, color = colors.muted)
                    }

                    Column {
                        Text(
                            text = note.title,
                            fontSize = 22.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        val preview = if (note.type == "checklist") {
                            "${note.items.count { it.done }}/${note.items.size} tasks completed"
                        } else {
                            note.body.ifBlank { "Tap to write..." }
                        }
                        Text(
                            text = preview,
                            fontSize = 13.sp,
                            color = colors.muted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Tick bar indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featured.size) { idx ->
                val active = idx == pagerState.currentPage
                val scaleY by animateFloatAsState(
                    targetValue = if (active) 1f else 0.35f,
                    animationSpec = NottoMotion.softSpring(),
                    label = "tickY"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(3.dp)
                        .height(16.dp)
                        .graphicsLayer {
                            this.scaleY = scaleY
                            alpha = if (active) 1f else 0.25f
                        }
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (active) colors.accent else colors.text)
                )
            }
        }
    }
}

@Composable
fun StackedNoteBand(
    note: NoteItem,
    index: Int,
    onClick: () -> Unit,
    onToggleCheck: (String) -> Unit
) {
    val colors = LocalNottoColors.current
    val grad = colors.cardGradients[note.toneIndex % colors.cardGradients.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x16322D64))
            .clip(RoundedCornerShape(26.dp))
            .background(grad)
            .pressable(onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(note.dateLabel, fontSize = 12.sp, color = colors.muted)
                }

                if (note.pinned) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (note.type == "checklist") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.items.take(3).forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface.copy(alpha = 0.65f))
                        ) {
                            AnimatedCheckRow(
                                text = item.text,
                                done = item.done,
                                onToggle = { onToggleCheck(item.id) }
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = note.body.ifBlank { "Tap to write..." },
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.text.copy(alpha = 0.72f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    )
                    // Fanned mini-cards
                    Box(modifier = Modifier.width(68.dp).height(44.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp, 42.dp)
                                .align(Alignment.BottomCenter)
                                .graphicsLayer { translationX = -14f; rotationZ = -8f }
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface.copy(alpha = 0.75f))
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp, 42.dp)
                                .align(Alignment.BottomCenter)
                                .graphicsLayer { translationX = 14f; rotationZ = 8f }
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface.copy(alpha = 0.85f))
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp, 44.dp)
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    notes: List<NoteItem>,
    onOpenNote: (NoteItem) -> Unit,
    onToggleCheck: (noteId: String, itemId: String) -> Unit
) {
    val colors = LocalNottoColors.current
    var query by remember { mutableStateOf("") }
    val results = remember(notes, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) notes
        else notes.filter { n ->
            n.title.lowercase().contains(q) ||
                n.body.lowercase().contains(q) ||
                n.items.any { it.text.lowercase().contains(q) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 112.dp)
    ) {
        item(key = "search_head") {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Search",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp,
                    color = colors.text
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Search titles, tasks, notes...", color = colors.muted, fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(colors.accent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = colors.muted,
                            modifier = Modifier
                                .size(20.dp)
                                .pressable { query = "" }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("${results.size} matches", fontSize = 12.sp, color = colors.muted)
                Spacer(Modifier.height(10.dp))
            }
        }

        itemsIndexed(results, key = { _, n -> n.id }) { idx, note ->
            StackedNoteBand(
                note = note,
                index = idx,
                onClick = { onOpenNote(note) },
                onToggleCheck = { itemId -> onToggleCheck(note.id, itemId) }
            )
        }
    }
}
