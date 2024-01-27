package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Abstract resource patch that allows Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param spoofedPackageSignature The signature of the package to spoof to.
 * @param dependencies Additional dependencies of this patch.
 */
abstract class BaseGmsCoreSupportResourcePatch(
    private val fromPackageName: String,
    private val toPackageName: String,
    private val spoofedPackageSignature: String,
    dependencies: Set<PatchClass> = setOf()
) : ResourcePatch(dependencies = setOf(ChangePackageNamePatch::class, AddResourcesPatch::class) + dependencies) {
    internal val gmsCoreVendorOption = stringPatchOption(
        key = "gmsCoreVendor",
        default = "com.mgoogle",
        values = mapOf(
            "Vanced" to "com.mgoogle",
            "ReVanced" to "app.revanced"
        ),
        title = "GmsCore Vendor",
        description = "The group id of the GmsCore vendor.",
        required = true
    ) { it!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")) }

    protected val gmsCoreVendor by gmsCoreVendorOption

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(BaseGmsCoreSupportResourcePatch::class)

        context.patchManifest()
        context.addSpoofingMetadata()
    }

    /**
     * Add metadata to manifest to support spoofing the package name and signature of GmsCore.
     */
    private fun ResourceContext.addSpoofingMetadata() {
        fun Node.adoptChild(tagName: String, block: Element.() -> Unit) {
            val child = ownerDocument.createElement(tagName)
            child.block()
            appendChild(child)
        }

        xmlEditor["AndroidManifest.xml"].use {
            val applicationNode = it
                .file
                .getElementsByTagName("application")
                .item(0)

            // Spoof package name and signature.
            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", "$gmsCoreVendor.android.gms.SPOOFED_PACKAGE_NAME")
                setAttribute("android:value", fromPackageName)
            }

            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", "$gmsCoreVendor.android.gms.SPOOFED_PACKAGE_SIGNATURE")
                setAttribute("android:value", spoofedPackageSignature)
            }

            // GmsCore presence detection in ReVanced Integrations.
            applicationNode.adoptChild("meta-data") {
                // TODO: The name of this metadata should be dynamic.
                setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                setAttribute("android:value", "$gmsCoreVendor.android.gms")
            }
        }
    }

    /**
     * Patch the manifest to support GmsCore.
     */
    private fun ResourceContext.patchManifest() {
        val packageName = ChangePackageNamePatch.setOrGetFallbackPackageName(toPackageName)

        val manifest = this["AndroidManifest.xml"].readText()
        this["AndroidManifest.xml"].writeText(
            manifest.replace(
                "package=\"$fromPackageName",
                "package=\"$packageName"
            ).replace(
                "android:authorities=\"$fromPackageName",
                "android:authorities=\"$packageName"
            ).replace(
                "$fromPackageName.permission.C2D_MESSAGE",
                "$packageName.permission.C2D_MESSAGE"
            ).replace(
                "$fromPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                "$packageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
            ).replace(
                "com.google.android.c2dm",
                "$gmsCoreVendor.android.c2dm"
            ).replace(
                "</queries>",
                "<package android:name=\"$gmsCoreVendor.android.gms\"/></queries>"
            )
        )
    }
}