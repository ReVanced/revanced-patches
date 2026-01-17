package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        maxMediationMethod.returnEarly()
        admobMediationMethod.returnEarly()
    }
}
