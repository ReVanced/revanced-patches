package app.revanced.patches.instagram.story.locationsticker

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val enableLocationStickerRedesignPatch = bytecodePatch(
    name = "Enable location sticker redesign",
    description = "Unlocks the redesigned location sticker with additional style options.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        locationStickerRedesignGateMethod.method.apply {
            // The gate method reads a MobileConfig boolean flag and returns it directly (6 instructions total).
            // Replacing the move-result at index 4 with a hardcoded true skips the flag check entirely,
            // enabling the redesigned sticker styles regardless of server configuration.
            replaceInstruction(4, "const/4 v0, 0x1")
        }
    }
}
