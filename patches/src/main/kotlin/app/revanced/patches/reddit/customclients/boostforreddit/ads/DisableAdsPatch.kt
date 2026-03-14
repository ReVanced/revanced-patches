package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch("Disable ads") {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        maxMediationMethod.returnEarly()
        admobMediationMethod.returnEarly()
    }
}
