package app.revanced.patches.all.misc.debugging

import app.revanced.patcher.patch.creatingResourcePatch
import org.w3c.dom.Element

@Suppress("unused", "ObjectPropertyName")
val `Enable Android debugging` by creatingResourcePatch(
    description = "Enables Android debugging capabilities. This can slow down the app.",
    use = false,
) {
    apply {
        document("AndroidManifest.xml").use { document ->
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            // set application as debuggable
            applicationNode.setAttribute("android:debuggable", "true")
        }
    }
}
