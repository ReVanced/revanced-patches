package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints.getMessagingCertFingerprint
import app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints.getReqistrationCertFingerprint

@Suppress("unused")
val firebaseGetCertPatch = bytecodePatch(
    description = "Spoofs the X-Android-Cert header.",
) {
    compatibleWith("de.dwd.warnapp"())

    val getRegistrationCertResult by getReqistrationCertFingerprint
    val getMessagingCertResult by getMessagingCertFingerprint

    execute {
        listOf(getRegistrationCertResult, getMessagingCertResult).forEach { method ->
            val spoofedInstruction =
                """
                    const-string v0, "0799DDF0414D3B3475E88743C91C0676793ED450"
                    return-object v0
                """

            method.mutableMethod.addInstructions(
                0,
                spoofedInstruction,
            )
        }
    }
}
