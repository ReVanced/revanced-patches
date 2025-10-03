package app.revanced.patches.music.misc.fileprovider

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.music.utils.fix.fileprovider.fileProviderResolverFingerprint

internal fun fileProviderPatch(
    youtubePackageName: String,
    musicPackageName: String
) = bytecodePatch(
    description = "Fixes broken YouTube Music file provider that prevents sharing with specific apps such as Instagram."
) {
    finalize {
        // Must do modification last, so change package name value is correctly set.
        val musicChangedPackageName = setOrGetFallbackPackageName(musicPackageName)

        // For some reason, if the app gets "android.support.FILE_PROVIDER_PATHS",
        // the package name of YouTube is used, not the package name of the YT Music.
        //
        // There is no issue in the stock YT Music, but this is an issue in the GmsCore Build.
        // https://github.com/ReVanced/revanced-patches/issues/55
        //
        // To solve this issue, replace the package name of YouTube with YT Music's package name.
        fileProviderResolverFingerprint.method.addInstructionsWithLabels(
            0,
            """
                const-string v0, "com.google.android.youtube.fileprovider"
                invoke-static { p1, v0 }, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
                move-result v0
                if-nez v0, :fix
                const-string v0, "$youtubePackageName.fileprovider"
                invoke-static { p1, v0 }, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
                move-result v0
                if-nez v0, :fix
                goto :ignore
                :fix
                const-string p1, "$musicChangedPackageName.fileprovider"
                :ignore
                nop
            """
        )
    }
}