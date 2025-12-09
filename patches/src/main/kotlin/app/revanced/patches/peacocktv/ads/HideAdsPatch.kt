package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Skips ads for movies and TV shows. Not tested for live TV.",
) {
    compatibleWith("com.peacocktv.peacockandroid")

    execute {
        mediaTailerAdServiceFingerprint.method.returnEarly(false)
    }
}
