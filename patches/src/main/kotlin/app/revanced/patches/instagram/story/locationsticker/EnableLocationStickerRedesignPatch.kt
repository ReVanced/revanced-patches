package app.revanced.patches.instagram.story.locationsticker

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableLocationStickerRedesignPatch = bytecodePatch(
    name = "Enable location sticker redesign",
    description = "Unlocks the redesigned location sticker with additional style options.",
    use = false,
) {
    compatibleWith("com.instagram.android"("421.0.0.51.66"))

    apply {
        // The gate method reads a MobileConfig boolean flag and returns it directly.
        // Returning early with true bypasses the flag check entirely,
        // enabling the redesigned sticker styles regardless of server configuration.
        val patched = runCatching {
            locationStickerRedesignGateMethodMatch.method.returnEarly(true)
            true
        }.getOrDefault(false)

        if (!patched) {
            runCatching {
                locationStickerRedesignGateFallbackMethod.returnEarly(true)
            }
        }
    }
}
