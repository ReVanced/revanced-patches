package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.RESOLVE_S_LINK_METHOD
import app.revanced.patches.reddit.customclients.SET_ACCESS_TOKEN_METHOD
import app.revanced.patches.reddit.customclients.fixSLinksPatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.sharedExtensionPatch

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/syncforreddit/FixSLinksPatch;"

@Suppress("unused")
val fixSLinksPatch = fixSLinksPatch(
    extensionPatch = sharedExtensionPatch,
) {
    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val handleNavigationMatch by linkHelperOpenLinkFingerprint()
    val setAccessTokenMatch by setAuthorizationHeaderFingerprint()

    execute {
        // region Patch navigation handler.

        handleNavigationMatch.mutableMethod.apply {
            val urlRegister = "p3"
            val tempRegister = "v2"

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

        setAccessTokenMatch.mutableMethod.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->$SET_ACCESS_TOKEN_METHOD",
        )

        // endregion
    }
}
