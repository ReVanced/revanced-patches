package app.revanced.patches.shared.settings.util

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.PreferenceCategory
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import java.io.Closeable

internal abstract class AbstractPreferenceScreen(
    private val root: MutableList<Screen> = mutableListOf()
) : Closeable {

    override fun close() {
        if (root.isEmpty())
            return

        for (preference in root.sortedBy { it.title }) {
            commit(preference.transform())
        }
    }

    /**
     * Finalize and insert root preference into resource patch
     */
    abstract fun commit(screen: PreferenceScreen)

    open inner class Screen(
        key: String,
        title: String,
        val summary: String? = null,
        preferences: MutableList<BasePreference> = mutableListOf(),
        val categories: MutableList<Category> = mutableListOf()
    ) : BasePreferenceCollection(key, title, preferences) {
        override fun transform(): PreferenceScreen {
            return PreferenceScreen(
                key,
                StringResource("${key}_title", title),
                 preferences.sortedBy { it.title.value } +
                         categories.sortedBy { it.title }.map { it.transform() },
                summary?.let { summary ->
                    StringResource("${key}_summary", summary)
                }
            )
        }

        private fun ensureScreenInserted() {
            // Add to screens if not yet done
            if(!this@AbstractPreferenceScreen.root.contains(this))
                this@AbstractPreferenceScreen.root.add(this)
        }

        fun addPreferences(vararg preferences: BasePreference) {
            ensureScreenInserted()
            this.preferences.addAll(preferences)
        }

        open inner class Category(
            key: String,
            title: String,
            preferences: MutableList<BasePreference> = mutableListOf()
        ): BasePreferenceCollection(key, title, preferences) {
            override fun transform(): PreferenceCategory {
                return PreferenceCategory(
                    key,
                    StringResource("${key}_title", title),
                    preferences.sortedBy { it.title.value }
                )
            }

            fun addPreferences(vararg preferences: BasePreference) {
                ensureScreenInserted()

                // Add to categories if not yet done
                if(!this@Screen.categories.contains(this))
                    this@Screen.categories.add(this)

                this.preferences.addAll(preferences)
            }
        }
    }

    abstract class BasePreferenceCollection(
        val key: String,
        val title: String,
        val preferences: MutableList<BasePreference> = mutableListOf()
    ) {
        abstract fun transform(): BasePreference
    }
}