package app.revanced.patches.all.misc.debugging

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

val enableAndroidDebuggingPatch = resourcePatch(
    name = "Enable Android debugging",
    description = "Enables Android debugging capabilities. This can slow down the app.",
    use = false,
) {
    execute {
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
