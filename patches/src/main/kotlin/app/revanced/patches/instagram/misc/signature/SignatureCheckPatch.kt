package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val signatureCheckPatch = bytecodePatch(
    name = "Disable signature check",
    description = "Disables the signature check that can cause the app to crash on startup. " +
            "Including this patch may cause issues with sharing or opening external Instagram links.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        isValidSignatureMethodFingerprint
            .match(isValidSignatureClassFingerprint.classDef)
            .method
            .returnEarly(true)
    }
}
