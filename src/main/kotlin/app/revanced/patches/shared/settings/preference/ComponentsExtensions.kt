package app.revanced.patches.shared.settings.preference

import app.revanced.patches.shared.settings.preference.impl.StringResource
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Add a resource node child
 *
 * @param resource The resource to add.
 * @param resourceCallback Called when a resource has been processed.
 */
internal fun Node.addResource(resource: BaseResource, resourceCallback: (BaseResource) -> Unit = { }) {
    appendChild(resource.serialize(ownerDocument, resourceCallback))
}

/**
 * Add a preference node child to the settings.
 *
 * @param preference The preference to add.
 * @param resourceCallback Called when a resource has been processed.
 */
internal fun Node.addPreference(preference: BasePreference, resourceCallback: ((BaseResource) -> Unit) = { }) {
    appendChild(preference.serialize(ownerDocument, resourceCallback))
}

internal fun Element.addSummary(summaryResource: StringResource?, summaryType: SummaryType = SummaryType.DEFAULT) =
    summaryResource?.let { summary ->
        setAttribute("android:${summaryType.type}", "@string/${summary.name}")
    }

internal fun <T> Element.addDefault(default: T) {
    if (default is Boolean && !(default as Boolean)) return // No need to include the default, as no value already means 'false'
    default?.let {
        setAttribute(
            "android:defaultValue", when (it) {
                is Boolean -> it.toString()
                is String -> it
                else -> throw IllegalArgumentException("Unsupported default value type: ${it::class.java.name}")
            }
        )
    }
}