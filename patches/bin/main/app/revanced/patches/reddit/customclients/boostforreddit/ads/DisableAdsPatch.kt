package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
) {
    compatibleWith("com.rubenmayayo.reddit")

    execute {
        arrayOf(maxMediationFingerprint, admobMediationFingerprint).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }
    }
}
