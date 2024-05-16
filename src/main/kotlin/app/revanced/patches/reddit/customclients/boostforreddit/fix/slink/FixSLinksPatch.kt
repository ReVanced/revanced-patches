package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.NavigationFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.JRAWgetAccessTokenFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.IntegrationsPatch
import app.revanced.util.exception

@Patch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    compatiblePackages = [
        CompatiblePackage("com.rubenmayayo.reddit")
    ],
    requiresIntegrations = true,
    dependencies= [IntegrationsPatch::class]
)
object FixSLinksPatch : BytecodePatch(
    setOf(NavigationFingerprint, JRAWgetAccessTokenFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX = "Lapp/revanced/integrations/boostforreddit/FixSLinksPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"
    override fun execute(context: BytecodeContext) {
        NavigationFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0,
            """
            invoke-static { p0, p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->resolveSLink(Landroid/content/Context;Ljava/lang/String;)Z
            move-result v1
            if-eqz v1, :continue
            return v1
            :continue
            nop
            """
        ) ?: throw NavigationFingerprint.exception
        JRAWgetAccessTokenFingerprint.result?.mutableMethod?.addInstructions(
            3,
            """
                invoke-static { v0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->JrawHookGetAccessToken(Ljava/lang/String;)V
            """
        ) ?: throw JRAWgetAccessTokenFingerprint.exception
    }
}
