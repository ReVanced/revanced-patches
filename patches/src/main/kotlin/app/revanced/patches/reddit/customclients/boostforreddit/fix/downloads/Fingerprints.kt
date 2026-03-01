package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.downloadAudioMethodMatch by composingFirstMethod {
    instructions(
        "/DASH_audio.mp4"(),
        "/audio"(),
    )
}
