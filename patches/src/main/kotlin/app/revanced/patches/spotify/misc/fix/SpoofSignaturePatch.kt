package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Superseded by spoofClientPatch", ReplaceWith("spoofClientPatch"))
@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    description = "Spoofs the signature of the app fix various functions of the app.",
) {
    dependsOn(spoofClientPatch)
}
