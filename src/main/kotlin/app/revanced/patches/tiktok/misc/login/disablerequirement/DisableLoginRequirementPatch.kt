package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch(
    name = "Disable login requirement",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    val mandatoryLoginServiceFingerprintResult by mandatoryLoginServiceFingerprint()
    val mandatoryLoginServiceFingerprintResult2 by mandatoryLoginService2Fingerprint()

    execute {
        listOf(
            mandatoryLoginServiceFingerprintResult.mutableMethod,
            mandatoryLoginServiceFingerprintResult2.mutableMethod,
        ).forEach { method ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}
