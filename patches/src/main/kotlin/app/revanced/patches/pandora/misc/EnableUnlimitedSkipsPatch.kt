package app.revanced.patches.pandora.misc

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Enable Unlimited Skips` by creatingBytecodePatch {
    compatibleWith("com.pandora.android")

    apply {
        getSkipLimitBehaviorMethod.returnEarly("unlimited")
    }
}
