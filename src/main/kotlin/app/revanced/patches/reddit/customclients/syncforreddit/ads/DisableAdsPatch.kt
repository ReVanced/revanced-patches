package app.revanced.patches.reddit.customclients.syncforreddit.ads

import app.revanced.patches.reddit.customclients.ads.BaseDisableAdsPatch

@Suppress("unused")
object DisableAdsPatch : BaseDisableAdsPatch(
    compatiblePackages = setOf(CompatiblePackage("io.syncapps.lemmy_sync")),
)
