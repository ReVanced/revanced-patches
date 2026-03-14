package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
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

    apply {
        // region Patch navigation handler.

        val urlRegister = "p3"
        val tempRegister = "v2"

        linkHelperOpenLinkMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { $urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->$RESOLVE_S_LINK_METHOD
                move-result $tempRegister
                if-eqz $tempRegister, :continue
                return $tempRegister
            """,
            ExternalLabel("continue", linkHelperOpenLinkMethod.getInstruction(0)),
        )

        // endregion

        // region Patch set access token.

        setAuthorizationHeaderMethod.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->$SET_ACCESS_TOKEN_METHOD",
        )

        // endregion
    }
}
