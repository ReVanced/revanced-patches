package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
) {
    compatibleWith("com.rubenmayayo.reddit")

    val maxMediationMatch by maxMediationFingerprint()
    val admobMediationMatch by admobMediationFingerprint()

    execute {
        arrayOf(maxMediationMatch, admobMediationMatch).forEach {
            it.mutableMethod.addInstructions(0, "return-void")
        }
    }
}
