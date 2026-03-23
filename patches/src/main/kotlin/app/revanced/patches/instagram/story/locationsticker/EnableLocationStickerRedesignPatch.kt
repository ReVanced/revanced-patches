package app.revanced.patches.instagram.story.locationsticker

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableLocationStickerRedesignPatch = bytecodePatch(
    name = "Enable location sticker redesign",
    description = "Unlocks the redesigned location sticker with additional style options.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        locationStickerRedesignGateMethodMatch.method.returnEarly()
    }
}
