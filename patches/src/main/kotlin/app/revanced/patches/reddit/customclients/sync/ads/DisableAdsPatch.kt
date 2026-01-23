package app.revanced.patches.reddit.customclients.sync.ads

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

fun `Disable ads`(block: BytecodePatchBuilder.() -> Unit = {}) = creatingBytecodePatch {
    apply {
        isAdsEnabledMethod.returnEarly(false)
    }

    block()
}
