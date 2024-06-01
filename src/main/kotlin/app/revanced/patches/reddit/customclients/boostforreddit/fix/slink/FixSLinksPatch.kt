package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.customclients.BaseFixSLinksPatch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.GetOAuthAccessTokenFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.NavigationFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.IntegrationsPatch

@Suppress("unused")
object FixSLinksPatch : BaseFixSLinksPatch(
    navigationFingerprint = NavigationFingerprint,
    setAccessTokenFingerprint = GetOAuthAccessTokenFingerprint,
    compatiblePackages = setOf(CompatiblePackage("com.rubenmayayo.reddit")),
    dependencies = setOf(IntegrationsPatch::class),
) {
    override val integrationsClassDescriptor = "Lapp/revanced/integrations/boostforreddit/FixSLinksPatch;"

    override fun MethodFingerprintResult.patchNavigation(context: BytecodeContext) {
        mutableMethod.apply {
            val contextRegister = "p0"
            val urlRegister = "p1"
            val tempRegister = "v1"

            addInstructionsWithLabels(
                0,
                """
                    invoke-static { $contextRegister, $urlRegister }, $resolveSLinkMethodDescriptor
                    move-result $tempRegister
                    if-eqz $tempRegister, :continue
                    return $tempRegister
                """,
                ExternalLabel("continue", getInstruction(0)),
            )
        }
    }

    override fun MethodFingerprintResult.patchSetAccessToken(context: BytecodeContext) = mutableMethod.addInstruction(
        3,
        "invoke-static { v0 }, $setAccessTokenMethodDescriptor",
    )
}
