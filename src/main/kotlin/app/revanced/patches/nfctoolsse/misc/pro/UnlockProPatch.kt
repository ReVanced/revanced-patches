package app.revanced.patches.nfctoolsse.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.nfctoolsse.misc.pro.fingerprints.isLicenseRegisteredFingerprint

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro"
) {
    compatibleWith("com.wakdev.apps.nfctools.se"())

    val isLicenseRegisteredResult by isLicenseRegisteredFingerprint

    execute {
        isLicenseRegisteredResult.mutableMethod
            .addInstructions(
                0, """
                    const/4 v0, 0x1
                    return v0
                """
            )
    }
}
