package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink.fingerprints.linkHelperOpenLinkFingerprint

@Suppress("unused")
val fixSLinksPatch = bytecodePatch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    requiresIntegrations = true,
) {
    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    val linkHelperOpenLinkResult by linkHelperOpenLinkFingerprint

    execute {
        linkHelperOpenLinkResult.mutableMethod.addInstructions(
            1,
            """
                invoke-static { p3 }, Lapp/revanced/integrations/syncforreddit/FixSLinksPatch;->resolveSLink(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p3
            """,
        )
    }
}
