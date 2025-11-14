package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val signatureCheckPatch = bytecodePatch(
    name = "Disable signature check",
    description = "Disables the signature check that can crash the app if external " +
            "Instagram links are opened with the app. Including this patch may prevent " +
            "sharing Instagram links from inside the app."
) {
    compatibleWith("com.instagram.android")

    execute {
        isValidSignatureMethodFingerprint
            .match(isValidSignatureClassFingerprint.classDef)
            .method
            .returnEarly(true)
    }
}
