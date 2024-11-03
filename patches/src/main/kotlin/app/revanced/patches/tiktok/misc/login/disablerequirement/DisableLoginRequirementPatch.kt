package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch(
    name = "Disable login requirement",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    execute {
        listOf(
            mandatoryLoginServiceFingerprint.matchOrThrow.method,
            mandatoryLoginService2Fingerprint.matchOrThrow.method,
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
