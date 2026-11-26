package com.notto.app.data

import android.content.Context
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Immutable
@Serializable
data class CheckItem(
    val id: String,
    val text: String,
    val done: Boolean = false
)

@Immutable
@Serializable
data class NoteItem(
    val id: String,
    val type: String = "note", // "note", "checklist", "visual"
    val toneIndex: Int = 0,
    val title: String,
    val body: String = "",
    val items: List<CheckItem> = emptyList(),
    val pinned: Boolean = false,
    val textSizeSp: Float = 16f,
    val dateLabel: String = "Today",
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class NotifConfig(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val title: String = "Time to jot!",
    val message: String = "Write one quick thought in Notto."
)

@Immutable
@Serializable
data class NottoState(
    val name: String = "Friend",
    val avatarEmoji: String = "🐣",
    val onboarded: Boolean = false,
    val darkTheme: Boolean = false,
    val sortAZ: Boolean = false,
    val notif: NotifConfig = NotifConfig(),
    val notes: List<NoteItem> = defaultSeedNotes()
)

fun defaultSeedNotes(): List<NoteItem> = listOf(
    NoteItem(
        id = "n1",
        type = "note",
        toneIndex = 0,
        title = "Design Principles",
        body = "Calm pastel surfaces, crisp 60fps spring physics, and zero clutter. Everything stays local and instant.",
        pinned = true,
        dateLabel = "Today",
        updatedAt = 1005L
    ),
    NoteItem(
        id = "n2",
        type = "checklist",
        toneIndex = 1,
        title = "Weekend Sprint",
        items = listOf(
            CheckItem("c1", "Tune 3D carousel depth", true),
            CheckItem("c2", "Add wave ruler haptics", true),
            CheckItem("c3", "Test on low-end device", false)
        ),
        pinned = true,
        dateLabel = "Today",
        updatedAt = 1004L
    ),
    NoteItem(
        id = "n3",
        type = "visual",
        toneIndex = 2,
        title = "Studio Moodboard",
        body = "Lavender mist, mint leaf, soft peach, sky blue. Smooth rounded geometry.",
        pinned = true,
        dateLabel = "26 Nov",
        updatedAt = 1003L
    ),
    NoteItem(
        id = "n4",
        type = "note",
        toneIndex = 3,
        title = "Book Quotes",
        body = "Simplicity is about subtracting the obvious and adding the meaningful. — John Maeda",
        pinned = false,
        dateLabel = "Yesterday",
        updatedAt = 1002L
    ),
    NoteItem(
        id = "n5",
        type = "checklist",
        toneIndex = 1,
        title = "Groceries & Matcha",
        items = listOf(
            CheckItem("g1", "Oat milk", false),
            CheckItem("g2", "Sourdough loaf", true),
            CheckItem("g3", "Ceremonial matcha", false)
        ),
        pinned = false,
        dateLabel = "25 Nov",
        updatedAt = 1001L
    )
)

class NottoStore(context: Context) {
    private val file = File(context.filesDir, "notto_state.json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun load(): NottoState {
        return try {
            if (file.exists()) {
                json.decodeFromString<NottoState>(file.readText())
            } else {
                NottoState()
            }
        } catch (_: Exception) {
            NottoState()
        }
    }

    fun save(state: NottoState) {
        scope.launch {
            try {
                file.writeText(json.encodeToString(state))
            } catch (_: Exception) {
            }
        }
    }
}
