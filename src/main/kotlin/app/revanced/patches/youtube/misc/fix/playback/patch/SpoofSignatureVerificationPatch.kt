package app.revanced.patches.youtube.misc.fix.playback.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.ProtobufParameterBuilderFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Name("spoof-signature-verification")
@Description("Spoofs a patched client to prevent playback issues.")
@DependsOn([
    IntegrationsPatch::class,
    SettingsPatch::class,
    PlayerTypeHookPatch::class,
])
@Version("0.0.1")
class SpoofSignatureVerificationPatch : BytecodePatch(
    listOf(
        ProtobufParameterBuilderFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_spoof_signature_verification",
                StringResource("revanced_spoof_signature_verification_title", "Spoof app signature"),
                StringResource("revanced_spoof_signature_verification_summary_on",
                    "App signature spoofed\\n\\n"
                        + "Side effects include:\\n"
                        + "• Ambient mode may not work\\n"
                        + "• Seekbar thumbnails are hidden\\n"
                        + "• Downloading videos may not work"),
                StringResource("revanced_spoof_signature_verification_summary_off", "App signature not spoofed"),
                StringResource("revanced_spoof_signature_verification_user_dialog_message",
                    "Turning off this setting may cause playback issues.")
            )
        )

        // hook parameter
        ProtobufParameterBuilderFingerprint.result?.let {
            val setParamMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex, true).getMethod() as MutableMethod

            setParamMethod.apply {
                val protobufParameterRegister = 3

                addInstructions(
                    0,
                    """
                        invoke-static {p$protobufParameterRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideProtobufParameter(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p$protobufParameterRegister
                    """
                )
            }
        } ?: return ProtobufParameterBuilderFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/SpoofSignatureVerificationPatch;"
    }
}
