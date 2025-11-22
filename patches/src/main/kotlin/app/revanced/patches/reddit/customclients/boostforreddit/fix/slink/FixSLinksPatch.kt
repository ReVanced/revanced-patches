package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patches.reddit.customclients.RESOLVE_S_LINK_METHOD
import app.revanced.patches.reddit.customclients.SET_ACCESS_TOKEN_METHOD
import app.revanced.patches.reddit.customclients.boostforreddit.misc.extension.sharedExtensionPatch
import app.revanced.patches.reddit.customclients.fixSLinksPatch

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/boostforreddit/FixSLinksPatch;"

@Suppress("unused")
val fixSlinksPatch = fixSLinksPatch(
    extensionPatch = sharedExtensionPatch,
) {
    compatibleWith("com.rubenmayayo.reddit")

    execute {
        // region Patch navigation handler.

        handleNavigationFingerprint.method.apply {
            val urlRegister = "p1"
            val tempRegister = "v1"

            addInstructionsWithLabels(
                0,
                """
                    invoke-static { $urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->$RESOLVE_S_LINK_METHOD
                    move-result $tempRegister
                    if-eqz $tempRegister, :continue
                    return $tempRegister
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        }

        // endregion

        // region Patch set access token.

        getOAuthAccessTokenFingerprint.method.addInstruction(
            3,
            "invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->$SET_ACCESS_TOKEN_METHOD",
        )

        // endregion
    }
}
