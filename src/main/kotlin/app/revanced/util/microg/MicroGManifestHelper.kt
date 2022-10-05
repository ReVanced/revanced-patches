package app.revanced.util.microg

import app.revanced.patcher.data.ResourceContext
import app.revanced.util.microg.Constants.META_GMS_PACKAGE_NAME
import app.revanced.util.microg.Constants.META_SPOOFED_PACKAGE_NAME
import app.revanced.util.microg.Constants.META_SPOOFED_PACKAGE_SIGNATURE
import app.revanced.util.microg.Constants.MICROG_VENDOR
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * helper class for adding manifest metadata needed for microG builds with signature spoofing
 */
internal object MicroGManifestHelper {

    /**
     * Add manifest entries needed for package and signature spoofing when using MicroG.
     * Note: this only adds metadata entries for signature spoofing, other changes may still be required to make a microG patch work.
     *
     * @param context Resource context.
     * @param spoofedPackage The package to spoof.
     * @param spoofedSignature The signature to spoof.
     */
    fun addSpoofingMetadata(
        context: ResourceContext,
        spoofedPackage: String,
        spoofedSignature: String
    ) {
        context.xmlEditor["AndroidManifest.xml"].use {
            val applicationNode = it
                .file
                .getElementsByTagName("application")
                .item(0)

            // package spoofing
            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", META_SPOOFED_PACKAGE_NAME)
                setAttribute("android:value", spoofedPackage)
            }
            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", META_SPOOFED_PACKAGE_SIGNATURE)
                setAttribute("android:value", spoofedSignature)
            }

            // microG presence detection in integrations
            applicationNode.adoptChild("meta-data") {
                setAttribute("android:name", META_GMS_PACKAGE_NAME)
                setAttribute("android:value", "${MICROG_VENDOR}.android.gms")
            }
        }
    }

    private fun Node.adoptChild(tagName: String, block: Element.() -> Unit) {
        val child = ownerDocument.createElement(tagName)
        child.block()
        appendChild(child)
    }
}