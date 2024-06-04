package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.packagename.changePackageNamePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Abstract resource patch that allows Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param spoofedPackageSignature The signature of the package to spoof to.
 * @param gmsCoreVendorGroupIdOption The option to get the vendor group ID of GmsCore.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
fun gmsCoreSupportResourcePatch(
    fromPackageName: String,
    toPackageName: String,
    spoofedPackageSignature: String,
    gmsCoreVendorGroupIdOption: Option<String>,
    executeBlock: Patch<ResourcePatchContext>.(ResourcePatchContext) -> Unit = {},
    block: ResourcePatchBuilder.() -> Unit = {},
) = resourcePatch {
    dependsOn(
        changePackageNamePatch,
        addResourcesPatch,
    )

    val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    execute { context ->
        addResources("shared", "misc.gms.BaseGmsCoreSupportResourcePatch")

        /**
         * Add metadata to manifest to support spoofing the package name and signature of GmsCore.
         */
        fun addSpoofingMetadata() {
            fun Node.adoptChild(
                tagName: String,
                block: Element.() -> Unit,
            ) {
                val child = ownerDocument.createElement(tagName)
                child.block()
                appendChild(child)
            }

            context.document["AndroidManifest.xml"].use { document ->
                val applicationNode =
                    document
                        .getElementsByTagName("application")
                        .item(0)

                // Spoof package name and signature.
                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_NAME")
                    setAttribute("android:value", fromPackageName)
                }

                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_SIGNATURE")
                    setAttribute("android:value", spoofedPackageSignature)
                }

                // GmsCore presence detection in ReVanced Integrations.
                applicationNode.adoptChild("meta-data") {
                    // TODO: The name of this metadata should be dynamic.
                    setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                    setAttribute("android:value", "$gmsCoreVendorGroupId.android.gms")
                }
            }
        }

        /**
         * Patch the manifest to support GmsCore.
         */
        fun patchManifest() {
            val packageName = setOrGetFallbackPackageName(toPackageName)

            val manifest = context["AndroidManifest.xml"].readText()
            context["AndroidManifest.xml"].writeText(
                manifest.replace(
                    "package=\"$fromPackageName",
                    "package=\"$packageName",
                ).replace(
                    "android:authorities=\"$fromPackageName",
                    "android:authorities=\"$packageName",
                ).replace(
                    "$fromPackageName.permission.C2D_MESSAGE",
                    "$packageName.permission.C2D_MESSAGE",
                ).replace(
                    "$fromPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    "$packageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                ).replace(
                    "com.google.android.c2dm",
                    "$gmsCoreVendorGroupId.android.c2dm",
                ).replace(
                    "</queries>",
                    "<package android:name=\"$gmsCoreVendorGroupId.android.gms\"/></queries>",
                ),
            )
        }

        patchManifest()
        addSpoofingMetadata()

        executeBlock(context)
    }

    block()
}
