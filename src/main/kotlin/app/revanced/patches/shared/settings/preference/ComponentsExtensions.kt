package app.revanced.patches.shared.settings.preference

import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Add a resource node child
 *
 * @param resource The resource to add.
 */
internal fun Node.addResource(resource: BaseResource) {
    appendChild(resource.serialize(ownerDocument))
}

/**
 * Add a preference node child to the settings.
 *
 * @param preference The preference to add.
 */
internal fun Node.addPreference(preference: BasePreference) {
    appendChild(preference.serialize(ownerDocument))
}

internal fun Element.addSummary(summaryResourceKey: String?, summaryType: SummaryType = SummaryType.DEFAULT)  {
    if (summaryResourceKey != null) {
        setAttribute("android:${summaryType.type}", "@string/$summaryResourceKey")
    }
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

internal fun CharSequence.removePunctuation(): String {
    val punctuation = "\\p{P}+".toRegex()
    return this.replace(punctuation, "")
}