package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

val firebaseGetCertPatch = bytecodePatch(
    description = "Spoofs the X-Android-Cert header.",
) {
    compatibleWith("de.dwd.warnapp")

    execute {
        listOf(getRegistrationCertFingerprint, getMessagingCertFingerprint).forEach { match ->
            match.method().addInstructions(
                0,
                """
                    const-string v0, "0799DDF0414D3B3475E88743C91C0676793ED450"
                    return-object v0
                """,
            )
        }
    }
}
