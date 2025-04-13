package app.revanced.patches.all.misc.display

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.getNode
import org.w3c.dom.Element
import java.util.logging.Logger

@Suppress("unused")
val edgeToEdgeDisplayPatch = resourcePatch(
    name = "Disable edge-to-edge display",
    description = "Disables forced edge-to-edge display on Android 15+ by downgrading the target Android SDK version.",
    use = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            fun getLogger() = Logger.getLogger(this::class.java.name)

            // Ideally, this patch should only be applied when targetSdkVersion is 35 or greater.
            // Since ApkTool does not add targetSdkVersion to AndroidManifest, there is no way
            // to check targetSdkVersion.  Instead, check compileSdkVersion and print a warning.
            try {
                val manifestElement = document.getNode("manifest") as Element
                val compileSdkVersion = Integer.parseInt(
                    manifestElement.getAttribute("android:compileSdkVersion")
                )
                if (compileSdkVersion < 35) {
                    getLogger().warning(
                        "This app does not appear to use Android 15 edge-to-edge display " +
                                "(compileSdkVersion: $compileSdkVersion)"
                    )
                }
            } catch (_: Exception) {
                getLogger().warning("Could not check compileSdkVersion")
            }

            // Change targetSdkVersion to 34 (Android 14).
            document.getElementsByTagName("manifest").item(0).let {
                var element = it.ownerDocument.createElement("uses-sdk")
                element.setAttribute("android:targetSdkVersion", "34")

                it.appendChild(element)
            }
        }
    }
}
