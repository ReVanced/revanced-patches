package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Superseded by spoofClientPatch", ReplaceWith("spoofClientPatch"))
@Suppress("unused")
val spoofPackageInfoPatch = bytecodePatch(
    description = "Spoofs the package info of the app to fix various functions of the app.",
) {
    dependsOn(spoofClientPatch)
}
