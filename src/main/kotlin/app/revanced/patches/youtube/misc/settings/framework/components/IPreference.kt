package app.revanced.patches.youtube.misc.settings.framework.components

import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource

/**
 * Preference
 */
internal interface IPreference {
    /**
     * Key of the preference.
     */
    val key: String

    /**
     * Title of the preference.
     */
    val title: StringResource

    /**
     * Tag name of the preference.
     */
    val tag: String
}