package app.revanced.patches.pandora.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAudioAdsPatch = bytecodePatch(
    name = "Disable audio ads",
) {
    compatibleWith("com.pandora.android")

    apply {
        getIsAdSupportedFingerprint.method.returnEarly(false)
        requestAudioAdFingerprint.method.returnEarly()
    }
}
