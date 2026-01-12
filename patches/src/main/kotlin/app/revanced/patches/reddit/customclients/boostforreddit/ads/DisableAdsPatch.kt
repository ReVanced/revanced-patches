package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        arrayOf(maxMediationMethod, admobMediationMethod).forEach { method ->
            method.addInstructions(0, "return-void")
        }
    }
}
