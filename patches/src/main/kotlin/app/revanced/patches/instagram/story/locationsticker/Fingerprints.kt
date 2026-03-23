package app.revanced.patches.instagram.story.locationsticker

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.locationStickerRedesignGateMethodMatch by composingFirstMethod {
    instructions("location_sticker_redesign_default"())
}
