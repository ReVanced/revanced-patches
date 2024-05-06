package app.revanced.patches.candylinkvpn

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.candylinkvpn.fingerprints.isPremiumPurchasedFingerprint

@Suppress("unused")
val UnlockProPatch = bytecodePatch(
    name = "Unlock pro"
) {
    compatibleWith("com.candylink.openvpn"())

    val isLicenseRegisteredResult by isPremiumPurchasedFingerprint

    execute {
        isLicenseRegisteredResult.mutableMethod.addInstructions(
            0,
            """
               const/4 v0, 0x1
               return v0
            """
        )
    }
}