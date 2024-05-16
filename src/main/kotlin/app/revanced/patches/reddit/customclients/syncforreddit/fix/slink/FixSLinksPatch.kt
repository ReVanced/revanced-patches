package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.AccountSingletonSetAccessHeaderFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.LinkHelperOpenLinkFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.IntegrationsPatch
import app.revanced.util.exception

@Patch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    compatiblePackages = [
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev")
    ],
    requiresIntegrations = true,
    dependencies = [IntegrationsPatch::class]
)
object FixSLinksPatch : BytecodePatch(
    setOf(LinkHelperOpenLinkFingerprint, AccountSingletonSetAccessHeaderFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX = "Lapp/revanced/integrations/syncforreddit/FixSLinksPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"
    override fun execute(context: BytecodeContext) {
        LinkHelperOpenLinkFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0,
            """
            invoke-static { p0, p3 }, $INTEGRATIONS_CLASS_DESCRIPTOR->resolveSLink(Landroid/content/Context;Ljava/lang/String;)Z
            move-result v2
            if-eqz v2, :continue
            return v2
            :continue
            nop
            """
        ) ?: throw LinkHelperOpenLinkFingerprint.exception
        AccountSingletonSetAccessHeaderFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                invoke-static { p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->JrawHookGetAccessToken(Ljava/lang/String;)V
            """
        ) ?: throw AccountSingletonSetAccessHeaderFingerprint.exception
    }
}