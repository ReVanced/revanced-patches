package app.revanced.patches.meta.misc.signature

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val signatureCheckPatch = bytecodePatch(
    name = "Disable signature check",
    description = "Disables the signature check that causes the app to crash on startup."
) {
    compatibleWith("com.instagram.android")

    execute {
        isValidSignatureMethodFingerprint
            .match(isValidSignatureClassFingerprint.classDef)
            .method
            .returnEarly(true)
    }
}
