package app.revanced.patches.meta.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    /**
     * Patch is identical for both Instagram and Threads app.
     */
    compatibleWith(
        "com.instagram.android",
        "com.instagram.barcelona",
    )

    execute {
        adInjectorFingerprint.method.returnEarly(false)
    }
}
