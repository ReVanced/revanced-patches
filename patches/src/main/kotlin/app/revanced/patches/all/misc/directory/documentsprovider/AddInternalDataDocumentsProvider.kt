package app.revanced.patches.all.misc.directory.documentsprovider

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence

internal const val DOCUMENTS_PROVIDER_CLASS =
    "app.revanced.extension.all.directory.documentsprovider.InternalDataDocumentsProvider"

@Suppress("unused")
val internalDataDocumentsProviderExtensionPatch = bytecodePatch {
    extendWith("extensions/all/directory/documentsprovider.rve")
}

@Suppress("unused")
val internalDataDocumentsProviderPatch = resourcePatch(
    name = "Internal data documents provider",
    description = "Exports an documents provider that grants access to the internal data directory of this app" +
            "to file managers and other apps that support the Storage Access Framework.",
    use = false,
) {
    dependsOn(internalDataDocumentsProviderExtensionPatch)

    execute {
        document("AndroidManifest.xml").use { document ->
            // Check if the provider is already declared
            if(document.getElementsByTagName("provider")
                .asSequence()
                .any { it.attributes.getNamedItem("android:name")?.nodeValue == DOCUMENTS_PROVIDER_CLASS }) {
                return@execute
            }

            val authority =
                document.getElementsByTagName("manifest").item(0).attributes.getNamedItem("package").let {
                    // Select a URI authority name that is unique to the current app
                    "${it.nodeValue}.${DOCUMENTS_PROVIDER_CLASS}"
                }

            // Register the documents provider
            with(document.getElementsByTagName("application").item(0)) {
                document.createElement("provider").apply {
                    setAttribute("android:name", DOCUMENTS_PROVIDER_CLASS)
                    setAttribute("android:authorities", authority)
                    setAttribute("android:exported", "true")
                    setAttribute("android:grantUriPermissions", "true")
                    setAttribute("android:permission", "android.permission.MANAGE_DOCUMENTS")

                    document.createElement("intent-filter").apply {
                        document.createElement("action").apply {
                            setAttribute("android:name", "android.content.action.DOCUMENTS_PROVIDER")
                        }.let(this::appendChild)
                    }.let(this::appendChild)
                }.let(this::appendChild)
            }
        }
    }
}

