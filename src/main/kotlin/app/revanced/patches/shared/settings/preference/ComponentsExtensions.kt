package app.revanced.patches.shared.settings.preference

import app.revanced.util.resource.StringResource
import org.w3c.dom.Element

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

internal fun CharSequence.removePunctuation(): String {
    val punctuation = "\\p{P}+".toRegex()
    return this.replace(punctuation, "")
}
