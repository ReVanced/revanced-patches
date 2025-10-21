package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence
import org.w3c.dom.Element

@Suppress("unused")
internal val addManifestPermissionsPatch = resourcePatch {

    val requiredPermissions = listOf(
        "android.permission.READ_PHONE_STATE",
        "android.permission.FOREGROUND_SERVICE_MICROPHONE",
        "android.permission.RECORD_AUDIO",
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            document.getElementsByTagName("manifest").item(0).let { manifestEl ->

                // Check which permissions are missing
                val existingPermissionNames = document.getElementsByTagName("uses-permission").asSequence()
                    .mapNotNull { (it as? Element)?.getAttribute("android:name") }.toSet()
                val missingPermissions = requiredPermissions.filterNot { it in existingPermissionNames }

                // Then add them
                for (permission in missingPermissions) {
                    val element = document.createElement("uses-permission")
                    element.setAttribute("android:name", permission)
                    manifestEl.appendChild(element)
                }
            }
        }
    }
}
