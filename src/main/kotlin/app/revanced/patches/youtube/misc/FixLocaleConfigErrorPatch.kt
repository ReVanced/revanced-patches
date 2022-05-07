package app.revanced.patches.youtube.misc

import app.revanced.patcher.data.implementation.ResourceData
import app.revanced.patcher.patch.implementation.ResourcePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import org.w3c.dom.Element

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.youtube",
        listOf("17.14.35", "17.17.34")
    )
)
class FixLocaleConfigErrorPatch : ResourcePatch(
    PatchMetadata(
        "locale-config-fix",
        "Manifest attribute fix patch",
        "Fix an error when building the resources by patching the manifest file.",
        compatiblePackages,
        "0.0.1"
    ),
) {
    override fun execute(data: ResourceData): PatchResult {
        // create an xml editor instance
        val editor = data.getXmlEditor("AndroidManifest.xml")

        // edit the application nodes attribute...
        val applicationNode = editor
            .file
            .getElementsByTagName("application")
            .item(0) as Element

        // by replacing the attributes name
        val attribute = "android:localeConfig"
        applicationNode.setAttribute("localeConfig",  applicationNode.getAttribute(attribute))
        applicationNode.removeAttribute("android:localeConfig")

        // close & save the modified file
        editor.close()

        return PatchResultSuccess()
    }
}
