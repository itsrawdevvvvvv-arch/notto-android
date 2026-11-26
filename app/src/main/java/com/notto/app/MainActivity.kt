package com.notto.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.notto.app.data.CheckItem
import com.notto.app.data.NoteItem
import com.notto.app.data.NottoState
import com.notto.app.data.NottoStore
import com.notto.app.data.defaultSeedNotes
import com.notto.app.ui.components.FloatingPillNav
import com.notto.app.ui.screens.CreateNoteSheet
import com.notto.app.ui.screens.CustomNotificationSheet
import com.notto.app.ui.screens.EditorSheet
import com.notto.app.ui.screens.FocusScreen
import com.notto.app.ui.screens.HomeScreen
import com.notto.app.ui.screens.MascotLoaderOverlay
import com.notto.app.ui.screens.OnboardingOverlay
import com.notto.app.ui.screens.ProfileEditSheet
import com.notto.app.ui.screens.SearchScreen
import com.notto.app.ui.screens.SettingsScreen
import com.notto.app.ui.theme.DarkNottoColors
import com.notto.app.ui.theme.LightNottoColors
import com.notto.app.ui.theme.LocalNottoColors
import com.notto.app.ui.theme.NottoMotion

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotifChannel(this)
        val store = NottoStore(applicationContext)
        val initialState = store.load()

        setContent {
            NottoApp(
                initialState = initialState,
                onPersist = { store.save(it) }
            )
        }
    }
}

private fun createNotifChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel("notto_reminders", "Notto Reminders", NotificationManager.IMPORTANCE_HIGH)
        mgr.createNotificationChannel(ch)
    }
}

private fun showLocalNotification(context: Context, title: String, message: String) {
    val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val n = NotificationCompat.Builder(context, "notto_reminders")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .build()
    mgr.notify(1001, n)
}

@Composable
fun NottoApp(
    initialState: NottoState,
    onPersist: (NottoState) -> Unit
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(initialState) }
    fun updateState(transform: (NottoState) -> NottoState) {
        state = transform(state)
        onPersist(state)
    }

    var showLoader by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("All") }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showNotifSheet by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val sortedNotes = remember(state.notes, state.sortAZ) {
        if (state.sortAZ) state.notes.sortedBy { it.title.lowercase() }
        else state.notes.sortedByDescending { it.updatedAt }
    }

    val anySheetOpen = editingNote != null || showCreateSheet || showProfileSheet || showNotifSheet
    val bgScale by animateFloatAsState(
        targetValue = if (anySheetOpen) 0.93f else 1f,
        animationSpec = NottoMotion.softSpring(),
        label = "bgScale"
    )
    val bgOffsetY by animateFloatAsState(
        targetValue = if (anySheetOpen) 16f else 0f,
        animationSpec = NottoMotion.softSpring(),
        label = "bgOffset"
    )

    val colors = if (state.darkTheme) DarkNottoColors else LightNottoColors

    CompositionLocalProvider(LocalNottoColors provides colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main App Shell (recedes when a sheet is open)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = bgScale
                        scaleY = bgScale
                        translationY = bgOffsetY
                    }
                    .clip(if (anySheetOpen) RoundedCornerShape(32.dp) else RoundedCornerShape(0.dp))
                    .background(colors.bg)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = NottoMotion.standardTween(220),
                    label = "tabCrossfade"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            name = state.name,
                            avatarEmoji = state.avatarEmoji,
                            notes = sortedNotes,
                            selectedFilter = selectedFilter,
                            onSelectFilter = { selectedFilter = it },
                            onOpenSearch = { selectedTab = 1 },
                            onOpenProfile = { showProfileSheet = true },
                            onOpenNote = { editingNote = it },
                            onToggleCheck = { noteId, itemId ->
                                updateState { s ->
                                    s.copy(
                                        notes = s.notes.map { n ->
                                            if (n.id == noteId) {
                                                n.copy(
                                                    items = n.items.map { c ->
                                                        if (c.id == itemId) c.copy(done = !c.done) else c
                                                    },
                                                    updatedAt = System.currentTimeMillis()
                                                )
                                            } else n
                                        }
                                    )
                                }
                            }
                        )
                        1 -> SearchScreen(
                            notes = sortedNotes,
                            onOpenNote = { editingNote = it },
                            onToggleCheck = { noteId, itemId ->
                                updateState { s ->
                                    s.copy(
                                        notes = s.notes.map { n ->
                                            if (n.id == noteId) {
                                                n.copy(
                                                    items = n.items.map { c ->
                                                        if (c.id == itemId) c.copy(done = !c.done) else c
                                                    },
                                                    updatedAt = System.currentTimeMillis()
                                                )
                                            } else n
                                        }
                                    )
                                }
                            }
                        )
                        2 -> FocusScreen()
                        else -> SettingsScreen(
                            name = state.name,
                            avatarEmoji = state.avatarEmoji,
                            darkTheme = state.darkTheme,
                            sortAZ = state.sortAZ,
                            notif = state.notif,
                            onToggleTheme = { dark -> updateState { it.copy(darkTheme = dark) } },
                            onToggleSort = { az -> updateState { it.copy(sortAZ = az) } },
                            onOpenProfile = { showProfileSheet = true },
                            onOpenNotif = { showNotifSheet = true },
                            onResetDemo = {
                                updateState { it.copy(notes = defaultSeedNotes()) }
                                selectedTab = 0
                            }
                        )
                    }
                }

                FloatingPillNav(
                    selectedTab = selectedTab,
                    createOpen = showCreateSheet,
                    onSelectTab = { selectedTab = it },
                    onPlusClick = { showCreateSheet = !showCreateSheet },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 14.dp)
                )
            }

            // Sheets
            EditorSheet(
                note = editingNote,
                onDismissAndSave = { updated ->
                    updateState { s ->
                        s.copy(notes = s.notes.map { if (it.id == updated.id) updated else it })
                    }
                    editingNote = null
                },
                onDelete = { id ->
                    updateState { s -> s.copy(notes = s.notes.filterNot { it.id == id }) }
                    editingNote = null
                }
            )

            CreateNoteSheet(
                visible = showCreateSheet,
                onDismiss = { showCreateSheet = false },
                onCreate = { type ->
                    showCreateSheet = false
                    val newNote = NoteItem(
                        id = "n_${System.currentTimeMillis()}",
                        type = type,
                        toneIndex = (state.notes.size) % 4,
                        title = if (type == "checklist") "New Checklist" else "Untitled Note",
                        items = if (type == "checklist") listOf(CheckItem("c_1", "First task", false)) else emptyList(),
                        dateLabel = "Today",
                        updatedAt = System.currentTimeMillis()
                    )
                    updateState { s -> s.copy(notes = listOf(newNote) + s.notes) }
                    editingNote = newNote
                }
            )

            ProfileEditSheet(
                visible = showProfileSheet,
                currentName = state.name,
                currentEmoji = state.avatarEmoji,
                onDismiss = { showProfileSheet = false },
                onSave = { newName, newEmoji ->
                    updateState { it.copy(name = newName, avatarEmoji = newEmoji, onboarded = true) }
                    showProfileSheet = false
                }
            )

            CustomNotificationSheet(
                visible = showNotifSheet,
                config = state.notif,
                onDismiss = { showNotifSheet = false },
                onTestNow = { title, msg ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        showLocalNotification(context, title, msg)
                    }
                },
                onSave = { cfg ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    updateState { it.copy(notif = cfg) }
                    showNotifSheet = false
                }
            )

            // Onboarding & Mascot Loader
            OnboardingOverlay(
                visible = !showLoader && !state.onboarded,
                initialName = state.name,
                initialEmoji = state.avatarEmoji,
                onComplete = { name, emoji ->
                    updateState { it.copy(name = name, avatarEmoji = emoji, onboarded = true) }
                }
            )

            MascotLoaderOverlay(
                visible = showLoader,
                onDismiss = { showLoader = false }
            )
        }
    }
}
