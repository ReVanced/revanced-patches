package app.revanced.patches.all.misc.targetSdk

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.getNode
import org.w3c.dom.Element
import java.util.logging.Logger

@Suppress("unused")
val setTargetSdkVersion34 = resourcePatch(
    name = "Set target SDK version 34",
    description = "Changes the target SDK to version 34 (Android 14). " +
            "For devices running Android 15+, this will disable edge-to-edge display.",
    use = false,
) {
    execute {
        val targetSdkOverride = 34 // Android 14.

        document("AndroidManifest.xml").use { document ->
            fun getLogger() = Logger.getLogger(this::class.java.name)

            // Ideally, the override should only be applied if the existing target is higher.
            // But since ApkTool does not add targetSdkVersion to the decompiled AndroidManifest,
            // there is no way to check targetSdkVersion. Instead, check compileSdkVersion and print a warning.
            try {
                val manifestElement = document.getNode("manifest") as Element
                val compileSdkVersion = Integer.parseInt(
                    manifestElement.getAttribute("android:compileSdkVersion")
                )
                if (compileSdkVersion <= targetSdkOverride) {
                    getLogger().warning(
                        "This app does not appear to use a target SDK above $targetSdkOverride: " +
                                "(compileSdkVersion: $compileSdkVersion)"
                    )
                }
            } catch (_: Exception) {
                getLogger().warning("Could not check compileSdkVersion")
            }

            // Change targetSdkVersion to override value.
            document.getElementsByTagName("manifest").item(0).let {
                var element = it.ownerDocument.createElement("uses-sdk")
                element.setAttribute("android:targetSdkVersion", targetSdkOverride.toString())

                it.appendChild(element)
            }
        }
    }
}
