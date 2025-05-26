package app.revanced.patches.meta.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides ads in stories, discover, profile, etc. " +
        "An ad can still appear once when refreshing the home feed.",
) {
    compatibleWith(
        "com.instagram.android",
        "com.instagram.barcelona",
    )

    execute {
        adInjectorFingerprint.method.returnEarly(false)
    }
}
