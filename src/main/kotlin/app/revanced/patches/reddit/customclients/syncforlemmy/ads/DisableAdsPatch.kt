package app.revanced.patches.reddit.customclients.syncforlemmy.ads

import app.revanced.patches.reddit.customclients.ads.BaseDisableAdsPatch
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.DisablePiracyDetectionPatch

@Suppress("unused")
object DisableAdsPatch : BaseDisableAdsPatch(
    dependencies = setOf(DisablePiracyDetectionPatch::class),
    compatiblePackages = setOf(CompatiblePackage("com.laurencedawson.reddit_sync")),
)
