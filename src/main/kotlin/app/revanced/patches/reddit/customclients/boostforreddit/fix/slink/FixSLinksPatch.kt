package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.JRAWgetAccessTokenFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.NavigationFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.IntegrationsPatch
import app.revanced.util.exception

@Patch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    compatiblePackages = [
        CompatiblePackage("com.rubenmayayo.reddit"),
    ],
    requiresIntegrations = true,
    dependencies = [IntegrationsPatch::class],
)
@Suppress("unused")
object FixSLinksPatch : BytecodePatch(
    setOf(NavigationFingerprint, JRAWgetAccessTokenFingerprint),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/boostforreddit/FixSLinksPatch;"

    override fun execute(context: BytecodeContext) {
        NavigationFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { p0, p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->resolveSLink(Landroid/content/Context;Ljava/lang/String;)Z
                    move-result v1
                    if-eqz v1, :continue
                    return v1
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        } ?: throw NavigationFingerprint.exception

        JRAWgetAccessTokenFingerprint.result?.mutableMethod?.addInstruction(
            3,
            "invoke-static { v0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setAppAccessToken(Ljava/lang/String;)V",
        ) ?: throw JRAWgetAccessTokenFingerprint.exception
    }
}
