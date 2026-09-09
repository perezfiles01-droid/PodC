package dev.ahmedmohamed.hayaitts.core.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Navigation invariants for the PodC shell.
 *
 * The bottom bar is the app's whole information architecture, so it gets an
 * explicit, enumerated assertion rather than a "does not contain X" check:
 * adding a tab has to be a deliberate edit here, not something that lands by
 * accident. The tab list and the hosted-route list are both read out of the
 * real nav host source at test time, so destinations added later are covered
 * without touching this file's machinery.
 */
class NavGraphTest {

    private val sources by lazy { Konsist.scopeFromProduction() }

    private val navHost by lazy {
        sources.files.first { it.path.endsWith("HayaiTtsNavHost.kt") }
    }

    /** Route constants named in `Tab(Routes.X, …)` entries, in bar order. */
    private fun declaredTabs(): List<String> =
        Regex("""Tab\(\s*Routes\.([A-Z_]+)""")
            .findAll(navHost.text)
            .map { it.groupValues[1] }
            .toList()

    /** Route constants the graph actually hosts a `composable {}` for. */
    private fun hostedRoutes(): Set<String> =
        Regex("""composable\(\s*(?:route\s*=\s*)?Routes\.([A-Z_]+)""")
            .findAll(navHost.text)
            .map { it.groupValues[1] }
            .toSet()

    @Test
    fun `bottom bar exposes exactly the expected tabs`() {
        assertEquals(
            listOf("HOME", "LIBRARY", "SETTINGS"),
            declaredTabs(),
        )
    }

    @Test
    fun `no tab points at a destination the graph does not host`() {
        // Studio was retired in the PodC fork and Activity left the bar. A
        // sibling reintroducing either — or any future tab pointing at a
        // route with no composable — fails here rather than crashing at
        // navigation time.
        assertEquals(emptyList<String>(), declaredTabs().filterNot { it in hostedRoutes() })
    }

    @Test
    fun `the downloads deep link resolves to a hosted destination`() {
        // Activity is no longer a tab, but DownloadNotifications sends users
        // here by URI when a voice install finishes, and Settings offers the
        // same entry. Deleting or renaming the route would break both
        // silently, so assert the emitter and the host together.
        val uri = "hayaitts://downloads"
        val notifications = sources.files.first { it.path.endsWith("DownloadNotifications.kt") }
        assertTrue(
            "DownloadNotifications no longer emits $uri",
            notifications.text.contains(uri),
        )
        assertTrue(
            "No nav destination hosts the $uri deep link",
            Regex("""Routes\.ACTIVITY[\s\S]{0,400}?\Q""" + uri + """\E""")
                .containsMatchIn(navHost.text),
        )
        assertTrue(
            "Routes.ACTIVITY is declared but no longer hosted",
            "ACTIVITY" in hostedRoutes(),
        )
    }

    @Test
    fun `settings keeps an entry point to the downloads screen`() {
        // Removing the tab must not orphan the screen: the nav host has to
        // hand SettingsScreen a callback that navigates to it.
        assertTrue(
            "SettingsScreen is no longer given an onOpenDownloads callback",
            // Anchored on the SettingsScreen call site: LibraryScreen takes a
            // callback of the same name, so an unanchored search passes even
            // when the Settings entry is gone.
            Regex(
                """SettingsScreen\([\s\S]{0,600}?onOpenDownloads\s*=\s*\{\s*navController\.navigate\(Routes\.ACTIVITY\)""",
            ).containsMatchIn(navHost.text),
        )
    }

    @Test
    fun `browse stays reachable after losing its tab`() {
        // Browse is where voices are downloaded. It gave up its tab in the
        // PodC bar, so the only thing keeping it reachable is a callback from
        // Home and from Library — if both go, the download surface is
        // orphaned with no compile error to say so.
        val entries = Regex("""onBrowse\s*=\s*\{\s*navController\.navigate\(Routes\.BROWSE\)""")
            .findAll(navHost.text)
            .count()
        assertTrue("Nothing navigates to Routes.BROWSE any more", entries >= 1)
        assertTrue("Routes.BROWSE is no longer hosted", "BROWSE" in hostedRoutes())
    }

    @Test
    fun `the listening flow is connected end to end`() {
        // Upload -> Choose Voice -> Listen only works if each step actually
        // navigates to the next. A screen that exists but that nothing routes
        // to compiles perfectly well and is dead on the device, so assert the
        // two hops rather than the destinations existing.
        assertTrue(
            "Home no longer opens the narrator step",
            Regex("""onStoryClick\s*=\s*\{[^}]*Routes\.narrator\(""")
                .containsMatchIn(navHost.text),
        )
        assertTrue(
            "The narrator step no longer starts playback",
            Regex("""onStartListening\s*=\s*\{[\s\S]{0,200}?Routes\.player\(""")
                .containsMatchIn(navHost.text),
        )
        listOf("NARRATOR", "PLAYER").forEach { route ->
            assertTrue("Routes.$route is not hosted", route in hostedRoutes())
        }
    }

    @Test
    fun `no production code references the retired Studio surface`() {
        sources.files.assertFalse { file ->
            Regex("""Routes\.STUDIO|ui\.studio|StudioScreen""").containsMatchIn(file.text)
        }
    }
}
