package app.revanced.patches.gmxmail.freephone

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val forceFreePhonePatch = bytecodePatch(
    name = "Force FreePhone",
    description = "Enable the FreePhone menu also on non eSim hardware in the navigation drawer.",
) {
    compatibleWith("de.gmx.mobile.android.mail")

    execute {
        isEuiccEnabledFingerprint.method.returnEarly(true)
    }
}