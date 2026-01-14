package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke

internal val downloadAudioMethodMatch = firstMethodComposite {
    instructions(
        "/DASH_audio.mp4"(),
        "/audio"()
    )
}
