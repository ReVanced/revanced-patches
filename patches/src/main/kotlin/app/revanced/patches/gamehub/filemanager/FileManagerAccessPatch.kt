package app.revanced.patches.gamehub.filemanager

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.gamehub.misc.extension.sharedGamehubExtensionPatch
import app.revanced.util.asSequence
import app.revanced.util.getNode

private const val PROVIDER_CLASS =
    "app.revanced.extension.gamehub.filemanager.MTDataFilesProvider"
private const val WAKEUP_CLASS =
    "app.revanced.extension.gamehub.filemanager.MTDataFilesWakeUpActivity"

@Suppress("unused")
val fileManagerAccessPatch = resourcePatch(
    name = "File manager access",
    description = "Adds a DocumentsProvider so that MT File Manager and other Storage Access " +
        "Framework clients can browse the app's internal storage directories.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))
    dependsOn(sharedGamehubExtensionPatch)

    execute {
        document("AndroidManifest.xml").use { dom ->
            val manifestNode = dom.getNode("manifest")
            val packageAttr = manifestNode.attributes.getNamedItem("package")
                ?: throw IllegalStateException("AndroidManifest.xml is missing 'package' attribute")
            val packageName = packageAttr.nodeValue

            val providerAuthority = "$packageName.$PROVIDER_CLASS"
            val wakeUpTaskAffinity = "$packageName.MTDataFilesWakeUp"

            val applicationNode = dom.getNode("application")

            // Guard: skip if the provider is already registered (idempotency).
            val existingProviders = dom.getElementsByTagName("provider").asSequence()
            if (existingProviders.any {
                it.attributes.getNamedItem("android:name")?.nodeValue == PROVIDER_CLASS
            }) {
                return@execute
            }

            // Register the wake-up activity.
            dom.createElement("activity").apply {
                setAttribute("android:name", WAKEUP_CLASS)
                setAttribute("android:exported", "true")
                setAttribute("android:excludeFromRecents", "true")
                setAttribute("android:noHistory", "true")
                setAttribute("android:taskAffinity", wakeUpTaskAffinity)
            }.let(applicationNode::appendChild)

            // Register the documents provider.
            dom.createElement("provider").apply {
                setAttribute("android:name", PROVIDER_CLASS)
                setAttribute("android:authorities", providerAuthority)
                setAttribute("android:exported", "true")
                setAttribute("android:grantUriPermissions", "true")
                setAttribute("android:permission", "android.permission.MANAGE_DOCUMENTS")

                dom.createElement("intent-filter").apply {
                    dom.createElement("action").apply {
                        setAttribute("android:name", "android.content.action.DOCUMENTS_PROVIDER")
                    }.let(this::appendChild)
                }.let(this::appendChild)
            }.let(applicationNode::appendChild)
        }
    }
}
