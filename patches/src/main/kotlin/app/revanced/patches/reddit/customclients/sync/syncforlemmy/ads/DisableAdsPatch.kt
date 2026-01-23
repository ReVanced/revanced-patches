package app.revanced.patches.reddit.customclients.sync.syncforlemmy.ads

import app.revanced.patches.reddit.customclients.sync.ads.`Disable ads`
import app.revanced.patches.reddit.customclients.sync.detection.piracy.disablePiracyDetectionPatch

@Suppress("unused")
val disableAdsPatch = `Disable ads` {
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith("com.laurencedawson.reddit_sync")
}
