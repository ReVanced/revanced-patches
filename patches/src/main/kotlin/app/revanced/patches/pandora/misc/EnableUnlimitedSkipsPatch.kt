package app.revanced.patches.pandora.misc

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Enable Unlimited Skips` by creatingBytecodePatch(
    description = "Enable unlimited skips"
) {
    compatibleWith("com.pandora.android")

    apply {
        skipLimitBehaviorMethod.returnEarly("unlimited")
    }
}
