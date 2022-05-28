package app.revanced.patches.youtube.misc.microg.patch.resource

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.ResourceData
import app.revanced.patcher.patch.implementation.ResourcePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import app.revanced.patches.youtube.misc.microg.shared.Constants.BASE_MICROG_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME

// @Patch TODO: finish patch
@Name("microg-resource-patch")
@Description("Resource patch to allow YouTube ReVanced to run without root and under a different package name.")
@MicroGPatchCompatibility
@Version("0.0.1")
class MicroGResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val manifest = data.get("AndroidManifest.xml").readText()

        data.get("AndroidManifest.xml").writeText(
            manifest.replace(
                "package=\"com.google.android.youtube\"", "package=\"$REVANCED_PACKAGE_NAME\""
            ).replace(
                " android:label=\"@string/application_name\" ", " android:label=\"{APP_NAME}\" "
            ).replace(
                "<uses-permission android:name=\"com.google.android.youtube.permission.C2D_MESSAGE\"",
                "<uses-permission android:name=\"$REVANCED_PACKAGE_NAME.permission.C2D_MESSAGE\""
            ).replace(
                "<permission android:name=\"com.google.android.youtube.permission.C2D_MESSAGE\"",
                "<permission android:name=\"$REVANCED_PACKAGE_NAME.permission.C2D_MESSAGE\""
            ).replace(
                "<provider android:authorities=\"com.google.android.youtube.lifecycle-trojan\"",
                "<provider android:authorities=\"$REVANCED_PACKAGE_NAME.lifecycle-trojan\""
            ).replace(
                "\"com.google.android.youtube.fileprovider\"", "\"$REVANCED_PACKAGE_NAME.fileprovider\""
            ).replace(
                "<provider android:authorities=\"com.google.android.youtube.photopicker_images\"",
                "<provider android:authorities=\"$REVANCED_PACKAGE_NAME.photopicker_images\""
            ).replace("com.google.android.c2dm", "$BASE_MICROG_PACKAGE_NAME.android.c2dm").replace(
                "    </queries>",
                "        <package android:name=\"$BASE_MICROG_PACKAGE_NAME.android.gms\"/>\n    </queries>"
            )
        )

        val replacement = arrayOf(
            Pair(
                "com.google.android.youtube.SuggestionProvider", "$REVANCED_PACKAGE_NAME.SuggestionProvider"
            ), Pair(
                "com.google.android.youtube.fileprovider", "$REVANCED_PACKAGE_NAME.fileprovider"
            )
        )

        data.forEach {
            if (it.extension != "xml") return@forEach

            // TODO: use a reader and only replace strings where needed instead of reading & writing the entire file
            var content = it.readText()
            replacement.filter { translation -> content.contains(translation.first) }.forEach { translation ->
                content = content.replace(translation.first, translation.second)
            }
            it.writeText(content)
        }

        return PatchResultSuccess()
    }
}