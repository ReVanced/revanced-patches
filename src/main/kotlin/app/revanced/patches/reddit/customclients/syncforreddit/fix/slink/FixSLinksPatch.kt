package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.LinkHelperOpenLinkFingerprint
import app.revanced.util.exception

@Patch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    compatiblePackages = [
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev")
    ],
    requiresIntegrations = true
)
object FixSLinksPatch : BytecodePatch(
    setOf(LinkHelperOpenLinkFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        LinkHelperOpenLinkFingerprint.result?.mutableMethod?.addInstructions(
            1,
            """
                invoke-static { p3 }, Lapp/revanced/integrations/syncforreddit/FixSLinksPatch;->resolveSLink(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p3
            """
        ) ?: throw LinkHelperOpenLinkFingerprint.exception
}
