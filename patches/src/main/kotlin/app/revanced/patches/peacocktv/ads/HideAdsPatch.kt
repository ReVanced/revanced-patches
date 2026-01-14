package app.revanced.patches.peacocktv.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch(
    description = "Hides all video ads."
) {
    compatibleWith("com.peacocktv.peacockandroid")

    apply {
        mediaTailerAdServiceMethod.returnEarly(false)
    }
}
