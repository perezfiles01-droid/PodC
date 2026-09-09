package dev.ahmedmohamed.hayaitts.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ahmedmohamed.hayaitts.R
import dev.ahmedmohamed.hayaitts.ui.components.FeaturedVoiceCard
import dev.ahmedmohamed.hayaitts.ui.components.HayaiScreenChrome
import dev.ahmedmohamed.hayaitts.ui.library.LibraryViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * PodC's landing tab.
 *
 * The fork turns text files into narrated listening, so Home is the front
 * door of that flow rather than a voice list: app identity at the top, then
 * the featured narrators, then the way into the full catalog. The upload
 * card and the recent-files list slot in above Featured — they arrive with
 * the story repository.
 *
 * Voice state comes from [LibraryViewModel] rather than a Home-specific VM:
 * the installed list, favourites and ordering are the same data the Library
 * tab shows, and duplicating the flow-combining would let the two tabs
 * disagree about what is installed.
 */
@Composable
fun HomeScreen(
    onVoiceClick: (voiceId: String) -> Unit,
    onBrowse: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HayaiScreenChrome(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_tagline),
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
