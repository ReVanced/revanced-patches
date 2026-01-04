package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Element

@Suppress("unused")
val disableSentryTelemetryPatch = resourcePatch(
    name = "Disable Sentry telemetry",
    description = "Disables Sentry telemetry. See https://sentry.io/for/android/ for more information.",
    use = false,
) {
    execute {
        fun Element.replaceOrCreate(tagName: String, attributeName: String, attributeValue: String) {
            val childElements = getElementsByTagName(tagName).asSequence().filterIsInstance<Element>()
            val targetChild = childElements.find { childElement ->
                childElement.getAttribute("android:name") == attributeName
            }
            if (targetChild != null) {
                targetChild.setAttribute("android:value", attributeValue)
            } else {
                appendChild(ownerDocument.createElement(tagName).apply {
                    setAttribute("android:name", attributeName)
                    setAttribute("android:value", attributeValue)
                })
            }
        }

        document("AndroidManifest.xml").use { document ->
            val application = document.getNode("application") as Element
            application.replaceOrCreate("meta-data", "io.sentry.enabled", "false")
            application.replaceOrCreate("meta-data", "io.sentry.dsn", "")
        }
    }
}
