package app.revanced.patches.youtube.misc.settings.framework.components.impl

/**
 * A Preference object.
 *
 * @param title The title of the preference.
 * @param intent The intent of the preference.
 * @param summary The summary of the text preference.
 */
internal class Preference(
    val title: StringResource,
    val intent: Intent,
    val summary: StringResource? = null
) {
    val tag: String = "Preference"

    data class Intent(val targetPackage: String, val data: String, val targetClass: String)
}