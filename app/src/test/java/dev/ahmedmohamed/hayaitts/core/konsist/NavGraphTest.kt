package dev.ahmedmohamed.hayaitts.core.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Navigation invariants for the PodC shell.
 *
 * The bottom bar is the app's whole information architecture, so it gets an
 * explicit, enumerated assertion rather than a "does not contain X" check:
 * adding a tab has to be a deliberate edit here, not something that lands by
 * accident. Every tab set is read out of the real source file at test time,
 * so a tab added later is covered without touching this file's machinery.
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

    @Test
    fun `bottom bar exposes exactly the expected tabs`() {
        assertEquals(
            listOf("LIBRARY", "BROWSE", "ACTIVITY", "SETTINGS"),
            declaredTabs(),
        )
    }

    @Test
    fun `no tab points at a retired destination`() {
        // Studio was retired in the PodC fork. A sibling reintroducing it —
        // or any future tab pointing at a route the graph no longer hosts —
        // fails here rather than crashing at navigation time.
        val hosted = Regex("""composable\(\s*(?:route\s*=\s*)?Routes\.([A-Z_]+)""")
            .findAll(navHost.text)
            .map { it.groupValues[1] }
            .toSet()
        val orphans = declaredTabs().filterNot { it in hosted }
        assertEquals(emptyList<String>(), orphans)
    }

    @Test
    fun `no production code references the retired Studio surface`() {
        sources.files.assertFalse { file ->
            Regex("""Routes\.STUDIO|ui\.studio|StudioScreen""").containsMatchIn(file.text)
        }
    }
}
