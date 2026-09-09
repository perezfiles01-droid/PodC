package dev.ahmedmohamed.hayaitts.ui.player

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ahmedmohamed.hayaitts.R
import dev.ahmedmohamed.hayaitts.ui.components.HayaiScreenChrome
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Step 3: the listening screen.
 *
 * Playback starts on its own: reaching this screen is the instruction to
 * listen, so there is no "preparing" state and no first tap. The chapter is
 * the navigation unit; narration itself streams continuously across chapter
 * boundaries, a sentence-unit at a time.
 */
@Composable
fun PlayerScreen(
    storyId: Long,
    voiceId: String,
    sid: Int,
    onBack: () -> Unit,
) {
    val viewModel: PlayerViewModel = koinViewModel { parametersOf(storyId, voiceId, sid) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sleepMenuOpen by remember { mutableStateOf(false) }
    var speedMenuOpen by remember { mutableStateOf(false) }

    HayaiScreenChrome(
        title = state.title,
        subtitle = state.current?.title,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        },
    ) { topInset ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset),
        ) {
            if (state.failed) {
                Text(
                    text = stringResource(R.string.player_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )
            }

            Text(
                text = stringResource(
                    R.string.player_chapter_of,
                    (state.currentIndex + 1).coerceAtLeast(1),
                    state.chapters.size.coerceAtLeast(1),
                ),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )

            LinearProgressIndicator(
                progress = {
                    val total = state.chapters.size
                    if (total == 0) 0f else (state.currentIndex + 1f) / total
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    TextButton(onClick = { speedMenuOpen = true }) {
                        Text(stringResource(R.string.player_speed, formatSpeed(state.speed)))
                    }
                    DropdownMenu(
                        expanded = speedMenuOpen,
                        onDismissRequest = { speedMenuOpen = false },
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.player_speed, formatSpeed(speed)))
                                },
                                onClick = {
                                    speedMenuOpen = false
                                    viewModel.setSpeed(speed)
                                },
                            )
                        }
                    }
                }

                IconButton(
                    onClick = viewModel::skipToPrevious,
                    enabled = state.hasPrevious,
                ) {
                    Icon(
                        Icons.Outlined.SkipPrevious,
                        contentDescription = stringResource(R.string.player_action_previous),
                    )
                }

                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    enabled = state.chapters.isNotEmpty(),
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) {
                            Icons.Outlined.Pause
                        } else {
                            Icons.Outlined.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (state.isPlaying) {
                                R.string.player_action_pause
                            } else {
                                R.string.action_play
                            },
                        ),
                    )
                }

                IconButton(onClick = viewModel::skipToNext, enabled = state.hasNext) {
                    Icon(
                        Icons.Outlined.SkipNext,
                        contentDescription = stringResource(R.string.player_action_next),
                    )
                }

                Box {
                    IconButton(onClick = { sleepMenuOpen = true }) {
                        Icon(
                            Icons.Outlined.Bedtime,
                            contentDescription = stringResource(R.string.player_sleep_timer),
                        )
                    }
                    DropdownMenu(
                        expanded = sleepMenuOpen,
                        onDismissRequest = { sleepMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.player_sleep_off)) },
                            onClick = {
                                sleepMenuOpen = false
                                viewModel.setSleepTimer(null)
                            },
                        )
                        listOf(15, 30, 45, 60).forEach { minutes ->
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.player_sleep_minutes, minutes))
                                },
                                onClick = {
                                    sleepMenuOpen = false
                                    viewModel.setSleepTimer(minutes)
                                },
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.player_chapters),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                itemsIndexed(state.chapters, key = { index, _ -> "chapter_$index" }) { index, chapter ->
                    ListItem(
                        modifier = Modifier.clickable { viewModel.seekTo(index) },
                        headlineContent = { Text(chapter.title) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    R.string.player_chapter_of,
                                    index + 1,
                                    state.chapters.size,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

/** "1" / "1.25" — no trailing zero on whole speeds. */
private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
