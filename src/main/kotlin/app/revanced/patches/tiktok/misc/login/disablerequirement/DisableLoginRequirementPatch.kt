package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.mandatoryLoginServiceFingerprint
import app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints.mandatoryLoginServiceFingerprint2

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch(
    name = "Disable login requirement",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically"
    )

    val mandatoryLoginServiceResult by mandatoryLoginServiceFingerprint
    val mandatoryLoginServiceResult2 by mandatoryLoginServiceFingerprint2

    execute {
        listOf(
            mandatoryLoginServiceResult.mutableMethod,
            mandatoryLoginServiceResult2.mutableMethod
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
