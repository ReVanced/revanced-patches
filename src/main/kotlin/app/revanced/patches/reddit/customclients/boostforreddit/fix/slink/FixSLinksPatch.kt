package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.BaseFixSLinksPatch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.JRAWgetAccessTokenFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.NavigationFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.IntegrationsPatch

@Suppress("unused")
object FixSLinksPatch : BaseFixSLinksPatch(
    navigationFingerprint = setOf(NavigationFingerprint),
    setAccessTokenFingerprint = setOf(JRAWgetAccessTokenFingerprint),
    compatiblePackages = setOf(CompatiblePackage("com.rubenmayayo.reddit")),
    dependencies = setOf(IntegrationsPatch::class)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/boostforreddit/FixSLinksPatch;"

    override fun Set<MethodFingerprintResult>.patchNavigation(context: BytecodeContext) {
        first().mutableMethod.apply {
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
        }
    }

    override fun Set<MethodFingerprintResult>.patchSetAccessToken(context: BytecodeContext) {
        first().mutableMethod.addInstruction(
            3,
            "invoke-static { v0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setAppAccessToken(Ljava/lang/String;)V",
        )
    }
}
