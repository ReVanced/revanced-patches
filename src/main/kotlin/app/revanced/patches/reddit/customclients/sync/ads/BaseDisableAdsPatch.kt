package app.revanced.patches.reddit.customclients.sync.ads

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.sync.ads.fingerprints.isAdsEnabledFingerprint
import app.revanced.util.returnEarly

fun disableAdsPatch(block: BytecodePatchBuilder.() -> Unit = {}) = bytecodePatch(
    name = "Disable ads",
) {
    isAdsEnabledFingerprint()

    execute {
        listOf(isAdsEnabledFingerprint).returnEarly()
    }

    block()
}
