package dev.ahmedmohamed.hayaitts.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ahmedmohamed.hayaitts.R
import dev.ahmedmohamed.hayaitts.domain.model.Story
import dev.ahmedmohamed.hayaitts.ui.components.FeaturedVoiceCard
import dev.ahmedmohamed.hayaitts.ui.components.HayaiScreenChrome
import dev.ahmedmohamed.hayaitts.ui.library.LibraryViewModel
import java.text.DateFormat
import java.util.Date
import org.koin.androidx.compose.koinViewModel

/**
 * PodC's landing tab: upload a text file, pick up a recent one, or browse
 * narrators.
 *
 * Voice state comes from [LibraryViewModel] rather than a Home-specific VM —
 * the installed list, favourites and ordering are the same data the Library
 * tab shows, and a second flow-combine would let the two tabs disagree about
 * what is installed. Story state is [HomeViewModel]'s.
 */
@Composable
fun HomeScreen(
    onVoiceClick: (voiceId: String) -> Unit,
    onBrowse: () -> Unit,
    onOpenSettings: () -> Unit,
    onStoryClick: (storyId: Long) -> Unit = {},
    viewModel: LibraryViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val recent by homeViewModel.recent.collectAsStateWithLifecycle()
    val importError by homeViewModel.importError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var renaming by remember { mutableStateOf<Story?>(null) }

    // Any text MIME type; the extension rule in StoryLimits is the real gate,
    // because providers label .txt inconsistently (text/plain, text/*, and
    // application/octet-stream all show up in the wild).
    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) homeViewModel.import(uri.toString()) }

    val messageFor: (HomeViewModel.ImportError) -> Int = { error ->
        when (error) {
            HomeViewModel.ImportError.TooLarge -> R.string.home_import_failed_too_large
            HomeViewModel.ImportError.WrongType -> R.string.home_import_failed_type
            HomeViewModel.ImportError.Empty -> R.string.home_import_failed_empty
            HomeViewModel.ImportError.Unreadable -> R.string.home_import_failed_storage
        }
    }
    val errorMessage = importError?.let { stringResource(messageFor(it)) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            homeViewModel.consumeImportError()
        }
    }

    HayaiScreenChrome(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_tagline),
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.nav_profile),
                )
            }
        },
    ) { topInset ->
        val voices = state.orderedInstalled
        val featured = remember(voices, state.favorites) {
            voices
                .sortedByDescending { it.voiceId in state.favorites }
                .distinctBy { it.family }
                .take(5)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("upload_card") {
                UploadCard(onChooseFile = { pickFile.launch(arrayOf("text/*")) })
            }

            item("recent_header") {
                SectionHeader(text = stringResource(R.string.home_section_recent))
            }
            if (recent.isEmpty()) {
                item("recent_empty") {
                    Text(
                        text = stringResource(R.string.home_recent_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(recent, key = { "story_" + it.id }) { story ->
                    StoryRow(
                        story = story,
                        onClick = { onStoryClick(story.id) },
                        onRename = { renaming = story },
                        onDelete = { homeViewModel.delete(story.id) },
                    )
                }
            }

            if (featured.isNotEmpty()) {
                item("featured_header") {
                    SectionHeader(
                        text = stringResource(R.string.home_section_featured),
                        actionLabel = stringResource(R.string.home_see_all),
                        onAction = onBrowse,
                    )
                }
                item("featured_row") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        items(featured, key = { "home_feat_" + it.voiceId }) { voice ->
                            FeaturedVoiceCard(
                                voice = voice,
                                onClick = { onVoiceClick(voice.voiceId) },
                                onPlayPreview = { onVoiceClick(voice.voiceId) },
                            )
                        }
                    }
                }
            }

            item("browse_entry") {
                OutlinedButton(
                    onClick = onBrowse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                    Text(
                        text = stringResource(R.string.home_browse_voices),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }

    val target = renaming
    if (target != null) {
        RenameDialog(
            initial = target.title,
            onDismiss = { renaming = null },
            onConfirm = { newTitle ->
                homeViewModel.rename(target.id, newTitle)
                renaming = null
            },
        )
    }
}

@Composable
private fun UploadCard(onChooseFile: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.UploadFile,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.home_upload_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_upload_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onChooseFile) {
                Text(stringResource(R.string.home_choose_file))
            }
            Text(
                text = stringResource(R.string.home_upload_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StoryRow(
    story: Story,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
        headlineContent = { Text(story.title) },
        supportingContent = { Text(story.subtitle()) },
        trailingContent = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.home_story_rename)) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.home_story_delete)) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun RenameDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_rename_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
            ) { Text(stringResource(R.string.home_action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** "245 KB · Aug 12, 2026", matching the PodC row layout. */
private fun Story.subtitle(): String {
    val kb = sizeBytes / 1024
    val size = if (kb >= 1024) "%.1f MB".format(kb / 1024f) else "$kb KB"
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(importedAtMillis))
    return "$size  ·  $date"
}

/** Section label with an optional trailing text action, per the PodC layout. */
@Composable
private fun SectionHeader(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = TextUnit(1.2f, TextUnitType.Sp),
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
