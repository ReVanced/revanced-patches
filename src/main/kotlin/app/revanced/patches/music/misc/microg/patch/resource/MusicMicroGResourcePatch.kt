package app.revanced.patches.music.misc.microg.patch.resource

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility
import app.revanced.patches.music.misc.microg.shared.Constants.BASE_MICROG_PACKAGE_NAME
import app.revanced.patches.music.misc.microg.shared.Constants.REVANCED_MUSIC_APP_NAME
import app.revanced.patches.music.misc.microg.shared.Constants.REVANCED_MUSIC_PACKAGE_NAME

@Name("music-microg-resource-patch")
@Description("Resource patch to allow YouTube Music ReVanced to run without root and under a different package name.")
@MusicMicroGPatchCompatibility
@Version("0.0.1")
class MusicMicroGResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {

        val manifest = data["AndroidManifest.xml"].readText()

        data["AndroidManifest.xml"].writeText(
            manifest.replace(
                "package=\"com.google.android.apps.youtube.music", "package=\"$REVANCED_MUSIC_PACKAGE_NAME"
            ).replace(
                "android:label=\"@string/app_name", "android:label=\"$REVANCED_MUSIC_APP_NAME"
            ).replace(
                "android:label=\"@string/app_launcher_name", "android:label=\"$REVANCED_MUSIC_APP_NAME"
            ).replace(
                "android:authorities=\"com.google.android.apps.youtube.music", "android:authorities=\"$REVANCED_MUSIC_PACKAGE_NAME"
            ).replace(
                "com.google.android.apps.youtube.music.permission.C2D_MESSAGE", "$REVANCED_MUSIC_PACKAGE_NAME.permission.C2D_MESSAGE"
            ).replace(
                "com.google.android.c2dm", "$BASE_MICROG_PACKAGE_NAME.android.c2dm"
            ).replace(
                "</queries>", "<package android:name=\"$BASE_MICROG_PACKAGE_NAME.android.gms\"/></queries>"
            )
        )

        return PatchResultSuccess()
    }
}