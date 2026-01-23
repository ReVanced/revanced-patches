package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke

internal val videoUrlReadyToStringMethodMatch = firstMethodComposite {
    instructions("VideoUrlReady(url="(), ", enableAds="())
}
