package app.revanced.patches.reddit.customclients.sync.syncforlemmy.ads

import app.revanced.patches.reddit.customclients.sync.ads.disableAdsPatch
import app.revanced.patches.reddit.customclients.sync.detection.piracy.`Disable piracy detection`

@Suppress("unused")
val disableAdsPatch = disableAdsPatch {
    dependsOn(`Disable piracy detection`)

    compatibleWith("com.laurencedawson.reddit_sync")
}
