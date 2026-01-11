package app.revanced.patches.pandora.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable Audio Ads` by creatingBytecodePatch(
    description = "Disable audio ads"
) {
    compatibleWith("com.pandora.android")

    apply {
        getIsAdSupportedMethod.returnEarly(false)
        requestAudioAdMethod.returnEarly()
    }
}
