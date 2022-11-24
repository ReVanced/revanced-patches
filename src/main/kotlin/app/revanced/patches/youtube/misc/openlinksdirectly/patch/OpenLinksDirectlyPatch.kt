package app.revanced.patches.youtube.misc.openlinksdirectly.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.openlinksdirectly.annotations.OpenLinksDirectlyCompatibility
import app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints.OpenLinksDirectlyFingerprintPrimary
import app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints.OpenLinksDirectlyFingerprintSecondary
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.shared.settings.impl.StringResource
import app.revanced.patches.shared.settings.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.formats.Instruction11x
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("open-links-directly")
@Description("Bypasses redirect links and allows opening links directly.")
@OpenLinksDirectlyCompatibility
@Version("0.0.1")
class OpenLinksDirectlyPatch : BytecodePatch(
    listOf(
        OpenLinksDirectlyFingerprintPrimary, OpenLinksDirectlyFingerprintSecondary
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_uri_redirect",
                StringResource("revanced_uri_redirect_title", "Open links directly"),
                true,
                StringResource("revanced_uri_redirect_summary_on", "Enabled"),
                StringResource("revanced_uri_redirect_summary_off", "Disabled")
            )
        )

        OpenLinksDirectlyFingerprintPrimary.hookUriParser(true)
        OpenLinksDirectlyFingerprintSecondary.hookUriParser(false)

        return PatchResultSuccess()
    }
}

fun MethodFingerprint.hookUriParser(isPrimaryFingerprint: Boolean) {
    fun getTargetRegister(instruction: Instruction): Int {
        if (isPrimaryFingerprint) return (instruction as Instruction35c).registerC
        return (instruction as Instruction11x).registerA
    }
    with(this.result!!) {
        val startIndex = scanResult.patternScanResult!!.startIndex
        val instruction = method.implementation!!.instructions.elementAt(startIndex + 1)
        val insertIndex = if (isPrimaryFingerprint) 1 else 2
        val targetRegister = getTargetRegister(instruction)

        mutableMethod.addInstructions(
            startIndex + insertIndex, """
               invoke-static {v$targetRegister}, Lapp/revanced/integrations/patches/OpenLinksDirectlyPatch;->parseRedirectUri(Ljava/lang/String;)Ljava/lang/String;
               move-result-object v$targetRegister
            """
        )
    }
}
