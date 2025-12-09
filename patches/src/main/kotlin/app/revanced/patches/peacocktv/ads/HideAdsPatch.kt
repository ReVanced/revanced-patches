package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides all video ads.",
) {
    compatibleWith("com.peacocktv.peacockandroid")

    execute {
        mediaTailerAdServiceFingerprint.method.returnEarly(false)
    }
}
