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

    val mandatoryLoginServiceMatch by mandatoryLoginServiceFingerprint()
    val mandatoryLoginService2Match by mandatoryLoginService2Fingerprint()

    execute {
        listOf(
            mandatoryLoginServiceMatch.mutableMethod,
            mandatoryLoginService2Match.mutableMethod,
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
