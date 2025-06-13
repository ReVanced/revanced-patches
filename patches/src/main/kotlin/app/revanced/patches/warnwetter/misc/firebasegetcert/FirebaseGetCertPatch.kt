package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val firebaseGetCertPatch = bytecodePatch(
    description = "Spoofs the X-Android-Cert header.",
) {
    compatibleWith("de.dwd.warnapp")

    execute {
        listOf(getRegistrationCertFingerprint, getMessagingCertFingerprint).forEach { match ->
            match.method.returnEarly("0799DDF0414D3B3475E88743C91C0676793ED450")
        }
    }
}
