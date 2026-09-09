package dev.ahmedmohamed.hayaitts.ui.narrator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ahmedmohamed.hayaitts.R
import dev.ahmedmohamed.hayaitts.domain.model.NarratorFilter
import dev.ahmedmohamed.hayaitts.ui.components.HayaiScreenChrome
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Step 2 of the listening flow, per the PodC layout: a step rail, the filter
 * chips, the narrator list, and one primary action.
 *
 * The chip set follows [NarratorFilter] — see its doc for why the mockup's
 * "Youth" and "Narrator" chips are not among them.
 */
@Composable
fun NarratorScreen(
    storyId: Long,
    onBack: () -> Unit,
    onStartListening: (voiceId: String) -> Unit,
) {
    val viewModel: NarratorViewModel = koinViewModel { parametersOf(storyId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HayaiScreenChrome(
        title = state.storyTitle,
        subtitle = stringResource(R.string.narrator_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        },
    ) { topInset ->
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = topInset, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item("rail") { StepRail(currentStep = 2) }
                item("blurb") {
                    Text(
                        text = stringResource(R.string.narrator_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                }
                item("chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NarratorFilter.entries.forEach { chip ->
                            FilterChip(
                                selected = state.filter == chip,
                                onClick = { viewModel.setFilter(chip) },
                                label = { Text(stringResource(chip.labelRes())) },
                            )
                        }
                    }
                }

                if (state.voices.isEmpty()) {
                    item("empty") {
                        Text(
                            text = stringResource(R.string.narrator_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                        )
                    }
                } else {
                    items(state.voices, key = { "narrator_" + it.voiceId }) { voice ->
                        val selected = voice.voiceId == state.selectedVoiceId
                        ListItem(
                            modifier = Modifier.selectable(
                                selected = selected,
                                onClick = { viewModel.select(voice.voiceId) },
                            ),
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                )
                            },
                            headlineContent = { Text(voice.title) },
                            supportingContent = {
                                Text(voice.languages.joinToString("  ·  "))
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.select(voice.voiceId) },
                                )
                            },
                        )
                    }
                }
            }

            Button(
                onClick = { state.selectedVoiceId?.let(onStartListening) },
                enabled = state.canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text(stringResource(R.string.narrator_start))
            }
        }
    }
}

private fun NarratorFilter.labelRes(): Int = when (this) {
    NarratorFilter.ALL -> R.string.narrator_filter_all
    NarratorFilter.FEMALE -> R.string.narrator_filter_female
    NarratorFilter.MALE -> R.string.narrator_filter_male
    NarratorFilter.NEUTRAL -> R.string.narrator_filter_neutral
}

/** Upload → Choose Voice → Listen, with [currentStep] emphasised. */
@Composable
private fun StepRail(currentStep: Int) {
    val steps = listOf(
        R.string.narrator_step_upload,
        R.string.narrator_step_voice,
        R.string.narrator_step_listen,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, labelRes ->
            val stepNumber = index + 1
            val reached = stepNumber <= currentStep
            Text(
                text = "$stepNumber  " + stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = if (reached) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
