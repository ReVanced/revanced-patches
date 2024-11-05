package app.revanced.patches.reddit.customclients.sync.ads

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

fun disableAdsPatch(block: BytecodePatchBuilder.() -> Unit = {}) = bytecodePatch(
    name = "Disable ads",
) {
    execute {
        isAdsEnabledFingerprint.method.returnEarly()
    }

    block()
}
