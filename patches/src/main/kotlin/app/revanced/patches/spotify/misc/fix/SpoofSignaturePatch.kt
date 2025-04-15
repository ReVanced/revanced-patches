package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Superseded by spoofPackageInfoPatch", ReplaceWith("spoofPackageInfoPatch"))
@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    description = "Spoofs the signature of the app fix various functions of the app.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(spoofPackageInfoPatch)
}
