package app.revanced.patches.twitch.misc.settings.components

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.impl.PreferenceCategory
import app.revanced.patches.shared.settings.preference.impl.StringResource

/**
 * Customized preference category for Twitch.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param preferences Child preferences of this category.
 */
internal open class CustomPreferenceCategory(
    key: String,
    title: StringResource,
    preferences: List<BasePreference>
) : PreferenceCategory(key, title, preferences) {
    override val tag: String = "app.revanced.twitch.settingsmenu.preference.CustomPreferenceCategory"
}