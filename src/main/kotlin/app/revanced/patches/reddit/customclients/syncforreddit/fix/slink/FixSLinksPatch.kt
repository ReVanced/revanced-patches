package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.BaseFixSLinksPatch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.AccountSingletonSetAccessHeaderFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.LinkHelperOpenLinkFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.IntegrationsPatch

@Suppress("unused")
object FixSLinksPatch : BaseFixSLinksPatch(
    navigationFingerprint = setOf(LinkHelperOpenLinkFingerprint),
    setAccessTokenFingerprint = setOf(AccountSingletonSetAccessHeaderFingerprint),
    compatiblePackages = setOf(
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev"),
    ),
    dependencies = setOf(IntegrationsPatch::class),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/syncforreddit/FixSLinksPatch;"

    override fun Set<MethodFingerprintResult>.patchNavigation(context: BytecodeContext) {
        first().mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { p0, p3 }, $INTEGRATIONS_CLASS_DESCRIPTOR->resolveSLink(Landroid/content/Context;Ljava/lang/String;)Z
                    move-result v2
                    if-eqz v2, :continue
                    return v2
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        }
    }

    override fun Set<MethodFingerprintResult>.patchSetAccessToken(context: BytecodeContext) {
        first().mutableMethod.apply {
            addInstruction(
                0,
                "invoke-static { p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setAppAccessToken(Ljava/lang/String;)V",
            )
        }
    }
}
