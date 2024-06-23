package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.BaseFixSLinksPatch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.LinkHelperOpenLinkFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints.SetAuthorizationHeaderFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.IntegrationsPatch

@Suppress("unused")
object FixSLinksPatch : BaseFixSLinksPatch(
    handleNavigationFingerprint = LinkHelperOpenLinkFingerprint,
    setAccessTokenFingerprint = SetAuthorizationHeaderFingerprint,
    compatiblePackages = setOf(
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev"),
    ),
    dependencies = setOf(IntegrationsPatch::class),
) {
    override val integrationsClassDescriptor = "Lapp/revanced/integrations/syncforreddit/FixSLinksPatch;"

    override fun MethodFingerprintResult.patchNavigationHandler(context: BytecodeContext) {
        mutableMethod.apply {
            val urlRegister = "p3"
            val tempRegister = "v2"

            addInstructionsWithLabels(
                0,
                """
                    invoke-static { $urlRegister }, $integrationsClassDescriptor->$resolveSLinkMethod
                    move-result $tempRegister
                    if-eqz $tempRegister, :continue
                    return $tempRegister
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        }
    }

    override fun MethodFingerprintResult.patchSetAccessToken(context: BytecodeContext) = mutableMethod.addInstruction(
        0,
        "invoke-static { p0 }, $integrationsClassDescriptor->$setAccessTokenMethod",
    )
}
