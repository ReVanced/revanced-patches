package app.revanced.patches.pandora.misc

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableUnlimitedSkipsPatch = bytecodePatch(
    name = "Enable unlimited skips",
) {
    compatibleWith("com.pandora.android")

    apply {
        skipLimitBehaviorFingerprint.method.returnEarly("unlimited")
    }
}
