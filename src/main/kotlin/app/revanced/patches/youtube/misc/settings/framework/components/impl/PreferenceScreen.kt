package app.revanced.patches.youtube.misc.settings.framework.components.impl

import app.revanced.patches.youtube.misc.settings.framework.components.BasePreference

/**
 * Preference screen.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param preferences Child preferences of this screen.
 * @param summary The summary of the text preference.
 */
internal open class PreferenceScreen(
    key: String,
    title: StringResource,
    val preferences: List<BasePreference>,
    var summary: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "PreferenceScreen"
}