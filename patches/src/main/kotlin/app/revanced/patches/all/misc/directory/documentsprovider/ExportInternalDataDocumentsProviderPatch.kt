package app.revanced.patches.all.misc.directory.documentsprovider

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence
import app.revanced.util.getNode

@Suppress("unused")
val exportInternalDataDocumentsProviderPatch = resourcePatch(
    name = "Export internal data documents provider",
    description = "Exports a documents provider that grants access to the internal data directory of this app " +
        "to file managers and other apps that support the Storage Access Framework.",
    use = false,
) {
    dependsOn(
        bytecodePatch {
            extendWith("extensions/all/misc/directory/documentsprovider/export-internal-data-documents-provider.rve")
        },
    )

    execute {
        val documentsProviderClass =
            "app.revanced.extension.all.misc.directory.documentsprovider.InternalDataDocumentsProvider"

        document("AndroidManifest.xml").use { document ->
            // Check if the provider is already declared
            if (document.getElementsByTagName("provider")
                    .asSequence()
                    .any { it.attributes.getNamedItem("android:name")?.nodeValue == documentsProviderClass }
            ) {
                return@execute
            }

            val authority =
                document.getNode("manifest").attributes.getNamedItem("package").let {
                    // Select a URI authority name that is unique to the current app
                    "${it.nodeValue}.$documentsProviderClass"
                }

            // Register the documents provider
            with(document.getNode("application")) {
                document.createElement("provider").apply {
                    setAttribute("android:name", documentsProviderClass)
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
