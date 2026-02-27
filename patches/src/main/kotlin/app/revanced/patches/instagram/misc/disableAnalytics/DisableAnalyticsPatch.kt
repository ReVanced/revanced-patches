package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables analytics that are sent periodically.",
) {
    compatibleWith("com.instagram.android")

    apply {
        // Returns BOGUS as analytics url.
        instagramAnalyticsUrlBuilderMethod.addInstructions(
            0,
            """
                const-string v0, "BOGUS"
                return-object v0
            """
        )

        // Replaces analytics url as BOGUS.
        facebookAnalyticsUrlInitMethodMatch.let { match ->
            match.method.apply {
                val urlIndex = match[1]
                val register = getInstruction<OneRegisterInstruction>(urlIndex).registerA
                replaceInstruction(urlIndex, "const-string v$register, \"BOGUS\"")
            }
        }
    }
}
