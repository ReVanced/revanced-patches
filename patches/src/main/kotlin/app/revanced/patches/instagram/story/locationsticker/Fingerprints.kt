package app.revanced.patches.instagram.story.locationsticker

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

// MobileConfig boolean key that gates the redesigned location sticker styles.
// The method containing this constant reads the flag and returns it directly,
// making it the sole control point for the feature. The key is stable across
// app updates as MobileConfig keys are server-assigned constants.
private const val LOCATION_STICKER_REDESIGN_CONFIG_KEY = 0x8105a100041e0dL

internal val BytecodePatchContext.locationStickerRedesignGateMethodMatch by composingFirstMethod {
    instructions(LOCATION_STICKER_REDESIGN_CONFIG_KEY())
}
