package dev.ahmedmohamed.hayaitts.ui.nav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.ahmedmohamed.hayaitts.R
import dev.ahmedmohamed.hayaitts.ui.activity.ActivityScreen
import dev.ahmedmohamed.hayaitts.ui.browse.BrowseScreen
import dev.ahmedmohamed.hayaitts.ui.custom.CustomImportScreen
import dev.ahmedmohamed.hayaitts.ui.detail.VoiceDetailScreen
import dev.ahmedmohamed.hayaitts.ui.home.HomeScreen
import dev.ahmedmohamed.hayaitts.ui.narrator.NarratorScreen
import dev.ahmedmohamed.hayaitts.ui.player.PlayerScreen
import dev.ahmedmohamed.hayaitts.ui.library.LibraryScreen
import dev.ahmedmohamed.hayaitts.ui.settings.SettingsScreen
import java.net.URLEncoder

/**
 * Top-level navigation: a [NavigationBar] hosts five top-level destinations
 * (Home / Library / Profile). Browse and the download/activity screens are
 * pushed from those tabs rather than owning a tab of their own. Each tab
 * pushes detail
 * routes (voice detail, custom import) on top of itself without disturbing
 * the bottom bar.
 *
 * PodC has no first-launch flow, so every launch starts on Home.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HayaiTtsNavHost(
    navController: NavHostController,
    onOpenQuickSwitch: () -> Unit,
    startDestination: String = Routes.HOME,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val showBottomBar = currentRoute in BOTTOM_BAR_ROUTES

    Scaffold(
        // Each screen's `HayaiScreenChrome` (the floating pill bar) handles
        // its own status-bar inset. If we leave the default systemBars on
        // this outer Scaffold, the inner bar gets pushed down by the inset
        // *twice* and the area between the system status bar and the pill
        // reads as a filled "top app bar background". Restrict the outer
        // inset to the navigation bars so the bottom NavigationBar still
        // floats above the system nav.
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onVoiceClick = { id -> navController.navigate(Routes.voiceDetail(id)) },
                    onBrowse = { navController.navigate(Routes.BROWSE) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onStoryClick = { storyId -> navController.navigate(Routes.narrator(storyId)) },
                )
            }
            composable(
                route = Routes.NARRATOR,
                arguments = listOf(navArgument(Routes.ARG_STORY_ID) { type = NavType.LongType }),
            ) { entry ->
                val storyId = entry.arguments?.getLong(Routes.ARG_STORY_ID) ?: 0L
                NarratorScreen(
                    storyId = storyId,
                    onBack = { navController.popBackStack() },
                    onStartListening = { voiceId ->
                        navController.navigate(Routes.player(storyId, voiceId))
                    },
                )
            }
            composable(
                route = Routes.PLAYER,
                arguments = listOf(
                    navArgument(Routes.ARG_STORY_ID) { type = NavType.LongType },
                    navArgument(Routes.ARG_VOICE_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                PlayerScreen(
                    storyId = entry.arguments?.getLong(Routes.ARG_STORY_ID) ?: 0L,
                    voiceId = entry.arguments?.getString(Routes.ARG_VOICE_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onBrowse = { navController.navigate(Routes.BROWSE) },
                    onVoiceClick = { id -> navController.navigate(Routes.voiceDetail(id)) },
                    onImport = { uri -> navController.navigate(Routes.customImport(uri)) },
                    onOpenQuickSwitch = onOpenQuickSwitch,
                    onOpenDownloads = { navController.navigate(Routes.ACTIVITY) },
                )
            }
            composable(Routes.BROWSE) {
                BrowseScreen(
                    onBack = { navController.popBackStack() },
                    onVoiceClick = { id -> navController.navigate(Routes.voiceDetail(id)) },
                    onOpenQuickSwitch = onOpenQuickSwitch,
                )
            }
            composable(
                route = Routes.ACTIVITY,
                deepLinks = listOf(navDeepLink { uriPattern = "hayaitts://downloads" }),
            ) {
                ActivityScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    // Activity left the bottom bar in the PodC fork, but the
                    // download/extraction progress it shows is the only
                    // feedback during a multi-hundred-MB voice install, so
                    // Settings keeps an entry point to it. The route itself
                    // is unchanged, which is what keeps the
                    // `hayaitts://downloads` notification deep link resolving.
                    onOpenDownloads = { navController.navigate(Routes.ACTIVITY) },
                )
            }
            composable(
                route = Routes.VOICE_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_VOICE_ID) { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_VOICE_ID).orEmpty()
                VoiceDetailScreen(
                    voiceId = id,
                    onBack = { navController.popBackStack() },
                    onOpenQuickSwitch = onOpenQuickSwitch,
                    onOpenCloning = { navController.navigate(Routes.voiceCloning(id)) },
                )
            }
            composable(
                route = Routes.VOICE_CLONING,
                arguments = listOf(navArgument(Routes.ARG_VOICE_ID) { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_VOICE_ID).orEmpty()
                dev.ahmedmohamed.hayaitts.ui.cloning.VoiceCloningScreen(
                    voiceId = id,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.CUSTOM_IMPORT,
                arguments = listOf(navArgument(Routes.ARG_ENCODED_URI) { type = NavType.StringType }),
            ) { entry ->
                val encoded = entry.arguments?.getString(Routes.ARG_ENCODED_URI).orEmpty()
                CustomImportScreen(
                    encodedUri = encoded,
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}

private data class Tab(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
)

private val Tabs = listOf(
    Tab(Routes.HOME, Icons.Outlined.Home, R.string.nav_home),
    Tab(Routes.LIBRARY, Icons.Outlined.LibraryMusic, R.string.nav_library),
    // Settings is the Profile tab in PodC: same screen, same route, and it
    // carries the Downloads entry that replaced the Activity tab.
    Tab(Routes.SETTINGS, Icons.Outlined.Person, R.string.nav_profile),
)

private val BOTTOM_BAR_ROUTES = Tabs.map { it.route }.toSet()

object Routes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val BROWSE = "browse"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"
    const val ARG_VOICE_ID = "voiceId"
    const val VOICE_DETAIL = "voiceDetail/{$ARG_VOICE_ID}"
    const val VOICE_CLONING = "voiceCloning/{$ARG_VOICE_ID}"

    const val ARG_STORY_ID = "storyId"
    const val NARRATOR = "narrator/{$ARG_STORY_ID}"
    const val PLAYER = "player/{$ARG_STORY_ID}/{$ARG_VOICE_ID}"

    const val ARG_ENCODED_URI = "encodedUri"
    const val CUSTOM_IMPORT = "customImport/{$ARG_ENCODED_URI}"

    fun voiceDetail(voiceId: String): String = "voiceDetail/$voiceId"
    fun narrator(storyId: Long): String = "narrator/$storyId"
    fun player(storyId: Long, voiceId: String): String = "player/$storyId/$voiceId"
    fun voiceCloning(voiceId: String): String = "voiceCloning/$voiceId"

    /**
     * Picked SAF URIs are URL-encoded so the `content://...` literal doesn't
     * collide with route path parsing. The destination VM decodes once.
     */
    fun customImport(rawUri: String): String =
        "customImport/${URLEncoder.encode(rawUri, "UTF-8")}"
}
