package app.revanced.patches.youtube.misc.microg.patch.resource

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import app.revanced.patches.youtube.misc.microg.shared.Constants.BASE_MICROG_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_APP_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch

@Name("microg-resource-patch")
@DependsOn(FixLocaleConfigErrorPatch::class)
@DependsOn(SettingsResourcePatch::class)
@Description("Resource patch to allow YouTube ReVanced to run without root and under a different package name.")
@MicroGPatchCompatibility
@Version("0.0.1")
class MicroGResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        data.xmlEditor["res/xml/settings_fragment.xml"].use {
            val settingsElementIntent = it.file.createElement("intent")
            settingsElementIntent.setAttribute("android:targetPackage", "$BASE_MICROG_PACKAGE_NAME.android.gms")
            settingsElementIntent.setAttribute("android:targetClass", "org.microg.gms.ui.SettingsActivity")

            val settingsElement = it.file.createElement("Preference")
            settingsElement.setAttribute("android:title", "@string/microg_settings")
            settingsElement.appendChild(settingsElementIntent)

            it.file.firstChild.appendChild(settingsElement)
        }

        val settingsFragment = data["res/xml/settings_fragment.xml"]
        settingsFragment.writeText(
            settingsFragment.readText().replace(
                "android:targetPackage=\"com.google.android.youtube",
                "android:targetPackage=\"$REVANCED_PACKAGE_NAME"
            )
        )

        val manifest = data["AndroidManifest.xml"]
        manifest.writeText(
            manifest.readText()
                .replace(
                    "package=\"com.google.android.youtube", "package=\"$REVANCED_PACKAGE_NAME"
                ).replace(
                    "android:label=\"@string/application_name", "android:label=\"$REVANCED_APP_NAME"
                ).replace(
                    "android:authorities=\"com.google.android.youtube", "android:authorities=\"$REVANCED_PACKAGE_NAME"
                ).replace(
                    "com.google.android.youtube.permission.C2D_MESSAGE", "$REVANCED_PACKAGE_NAME.permission.C2D_MESSAGE"
                ).replace( // might not be needed
                    "com.google.android.youtube.lifecycle-trojan", "$REVANCED_PACKAGE_NAME.lifecycle-trojan"
                ).replace( // might not be needed
                    "com.google.android.youtube.photopicker_images", "$REVANCED_PACKAGE_NAME.photopicker_images"
                ).replace(
                    "com.google.android.c2dm", "$BASE_MICROG_PACKAGE_NAME.android.c2dm"
                ).replace(
                    "</queries>", "<package android:name=\"$BASE_MICROG_PACKAGE_NAME.android.gms\"/></queries>"
                )
        )

        return PatchResultSuccess()
    }
}