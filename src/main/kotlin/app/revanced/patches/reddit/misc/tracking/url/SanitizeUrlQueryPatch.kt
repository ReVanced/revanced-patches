package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.misc.tracking.url.fingerprints.ShareLinkFormatterFingerprint
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Sanitize sharing links",
    description = "Adds an option to remove tracking query parameters from URLs when sharing links.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object SanitizeUrlQueryPatch : BytecodePatch(
    setOf(ShareLinkFormatterFingerprint)
) {
    private const val SANITIZE_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/SanitizeUrlQueryPatch;" +
                "->stripQueryParameters()Z"

    override fun execute(context: BytecodeContext) {
        ShareLinkFormatterFingerprint.result?.let { result ->
            result.mutableMethod.apply {

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $SANITIZE_METHOD_DESCRIPTOR
                        move-result v0
                        if-eqz v0, :off
                        return-object p0
                        """, ExternalLabel("off", getInstruction(0))
                )
            }
        } ?: throw ShareLinkFormatterFingerprint.exception

        updateSettingsStatus("SanitizeUrlQuery")

    }
}
