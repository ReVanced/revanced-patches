package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.RESOLVE_S_LINK_METHOD
import app.revanced.patches.reddit.customclients.SET_ACCESS_TOKEN_METHOD
import app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.integrationsPatch
import app.revanced.patches.reddit.customclients.fixSLinksPatch

const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/boostforreddit/FixSLinksPatch;"

@Suppress("unused")
val fixSlinksPatch = fixSLinksPatch(
    integrationsPatch = integrationsPatch,
) {
    compatibleWith("com.rubenmayayo.reddit")

    val handleNavigationResult by handleNavigationFingerprint
    val setAccessTokenResult by getOAuthAccessTokenFingerprint

    execute {
        // region Patch navigation handler.

        handleNavigationResult.mutableMethod.apply {
            val urlRegister = "p1"
            val tempRegister = "v1"

            addInstructionsWithLabels(
                0,
                """
                    invoke-static { $urlRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->$RESOLVE_S_LINK_METHOD
                    move-result $tempRegister
                    if-eqz $tempRegister, :continue
                    return $tempRegister
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        }

        // endregion

        // region Patch set access token.

        setAccessTokenResult.mutableMethod.addInstruction(
            3,
            "invoke-static { v0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->$SET_ACCESS_TOKEN_METHOD",
        )

        // endregion
    }
}
