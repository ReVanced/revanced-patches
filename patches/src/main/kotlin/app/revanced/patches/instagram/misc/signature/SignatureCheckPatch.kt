package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Disable signature check` by creatingBytecodePatch(
    description = "Disables the signature check that can cause the app to crash on startup. " +
        "Using this patch may cause issues with sharing or opening external Instagram links.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        isValidSignatureMethodMethod
            .match(isValidSignatureClassMethod.classDef)
            .method
            .returnEarly(true)
    }
}
