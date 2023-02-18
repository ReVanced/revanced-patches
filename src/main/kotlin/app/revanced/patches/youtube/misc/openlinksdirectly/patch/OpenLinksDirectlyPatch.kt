package app.revanced.patches.youtube.misc.openlinksdirectly.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.openlinksdirectly.annotations.OpenLinksDirectlyCompatibility
import app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints.OpenLinksDirectlyPrimaryFingerprint
import app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints.OpenLinksDirectlySecondaryFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("open-links-directly")
@Description("Skips over redirection URLs to external links.")
@OpenLinksDirectlyCompatibility
@Version("0.0.1")
class OpenLinksDirectlyPatch : BytecodePatch(
    listOf(OpenLinksDirectlyPrimaryFingerprint, OpenLinksDirectlySecondaryFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_uri_redirect",
                StringResource("revanced_uri_redirect_title", "Bypass URL redirects"),
                true,
                StringResource("revanced_uri_redirect_summary_on", "Bypassing URL redirects"),
                StringResource("revanced_uri_redirect_summary_off", "Following default redirect policy")
            )
        )

        arrayOf(OpenLinksDirectlyPrimaryFingerprint, OpenLinksDirectlySecondaryFingerprint)
            .map { it.result ?: return it.toErrorResult() }
            .forEach { result ->
                result.mutableMethod.apply {
                    val insertIndex = result.scanResult.patternScanResult!!.startIndex
                    val uriRegister = (instruction(insertIndex) as Instruction35c).registerC
                    replaceInstruction(
                        insertIndex,
                        "invoke-static {v$uriRegister}," +
                                "Lapp/revanced/integrations/patches/OpenLinksDirectlyPatch;" +
                                "->" +
                                "transformRedirectUri(Ljava/lang/String;)Landroid/net/Uri;"
                    )
                }
            }
        return PatchResultSuccess()
    }
}