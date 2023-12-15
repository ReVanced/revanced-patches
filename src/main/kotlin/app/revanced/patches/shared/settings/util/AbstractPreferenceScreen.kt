package app.revanced.patches.shared.settings.util

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.PreferenceCategory
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import java.io.Closeable

abstract class AbstractPreferenceScreen(
    private val root: MutableList<Screen> = mutableListOf()
) : Closeable {

    override fun close() {
        if (root.isEmpty())
            return

        for (preference in root) {
            commit(preference.transform())
        }
    }

    /**
     * Finalize and insert root preference into resource patch
     */
    abstract fun commit(screen: PreferenceScreen)

    open inner class Screen(
        key: String,
        titleKey: String,
        val summaryKey: String? = null,
        preferences: MutableList<BasePreference> = mutableListOf(),
        val categories: MutableList<Category> = mutableListOf()
    ) : BasePreferenceCollection(key, titleKey, preferences) {
        override fun transform(): PreferenceScreen {
            return PreferenceScreen(
                key,
                titleKey,
                // Screens and preferences are sorted at runtime by integrations code,
                // so they appear in alphabetical order for the localized language in use.
                 preferences + categories.map { it.transform() },
                summaryKey
            )
        }

        private fun ensureScreenInserted() {
            // Add to screens if not yet done
            if (!this@AbstractPreferenceScreen.root.contains(this))
                this@AbstractPreferenceScreen.root.add(this)
        }

        fun addPreferences(vararg preferences: BasePreference) {
            ensureScreenInserted()
            this.preferences.addAll(preferences)
        }

        open inner class Category(
            key: String,
            titleKey: String,
            preferences: MutableList<BasePreference> = mutableListOf()
        ) : BasePreferenceCollection(key, titleKey, preferences) {
            override fun transform(): PreferenceCategory {
                return PreferenceCategory(
                    key,
                    titleKey,
                    preferences
                )
            }

            fun addPreferences(vararg preferences: BasePreference) {
                ensureScreenInserted()

                // Add to categories if not yet done
                if (!this@Screen.categories.contains(this))
                    this@Screen.categories.add(this)

                this.preferences.addAll(preferences)
            }
        }
    }

    abstract class BasePreferenceCollection(
        val key: String,
        val titleKey: String,
        val preferences: MutableList<BasePreference> = mutableListOf()
    ) {
        abstract fun transform(): BasePreference
    }
}