package app.revanced.patches.youtube.misc.video.speed.remember.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.video.speed.remember.annotation.RememberPlaybackRateCompatibility
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.ChangePlaybackRateFragmentStateFingerprint
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.InitializePlaybackRateValuesFingerprint
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.OnPlaybackRateItemClickFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@Patch
@Name("remember-playback-rate")
@Description("Adds the ability to remember the playback rate you chose in the video playback rate flyout.")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@RememberPlaybackRateCompatibility
@Version("0.0.1")
class RememberPlaybackRatePatch : BytecodePatch(
    listOf(ChangePlaybackRateFragmentStateFingerprint)
) {
    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/speed/RememberPlaybackRatePatch;"

        fun MethodFingerprint.getReference(offsetFromPatternScanResultStartIndex: Int = 0) = this.result!!.let {
            val referenceInstruction = it.mutableMethod
                .instruction(it.scanResult.patternScanResult!!.startIndex + offsetFromPatternScanResultStartIndex) as ReferenceInstruction
            referenceInstruction.reference.toString()
        }

        fun BytecodeContext.resolveFingerprints() {
            ChangePlaybackRateFragmentStateFingerprint.result?.also {
                fun MethodFingerprint.resolve() = resolve(this@resolveFingerprints, it.classDef)

                OnPlaybackRateItemClickFingerprint.resolve()
                InitializePlaybackRateValuesFingerprint.resolve()

            } ?: throw ChangePlaybackRateFragmentStateFingerprint.toErrorResult()
        }
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_playback_rate_last_selected",
                StringResource("revanced_remember_playback_rate_last_selected_title", "Remember playback rate changes"),
                true,
                StringResource(
                    "revanced_remember_playback_rate_last_selected_summary_on",
                    "Playback rate changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_playback_rate_last_selected_summary_off",
                    "Playback rate changes only apply to the current video"
                )
            )
        )

        context.resolveFingerprints()

        // Set the remembered playback rate.
        InitializePlaybackRateValuesFingerprint.result!!.apply {
            // Infer everything necessary for setPlaybackRate()

            val playbackHandlerWrapperFieldReference =
                (object : MethodFingerprint(opcodes = listOf(Opcode.IF_EQZ)) {}).apply {
                    OnPlaybackRateItemClickFingerprint.result!!.apply {
                        resolve(
                            context,
                            method,
                            classDef
                        )
                    }
                }.getReference(-1)
            val playbackHandlerWrapperImplementorClassReference = OnPlaybackRateItemClickFingerprint
                .getReference(-1)
            val playbackHandlerFieldReference = OnPlaybackRateItemClickFingerprint
                .getReference()
            val setPlaybackRateMethodReference = OnPlaybackRateItemClickFingerprint
                .getReference(1)

            mutableMethod.addInstructions(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getRememberedPlaybackRate()F
                    move-result v0
                    
                    # check if the playback rate is below 0 (when a playback rate was never remembered)
                    
                    const/4 v1, 0x0
                    cmpg-float v1, v0, v1
                    if-lez v1, :do_not_override

                    # invoke setPlaybackRate
                    
                    iget-object v1, p0, $playbackHandlerWrapperFieldReference 
                    check-cast v1, $playbackHandlerWrapperImplementorClassReference
                    iget-object v2, v1, $playbackHandlerFieldReference
                    invoke-virtual {v2, v0}, $setPlaybackRateMethodReference
                """.trimIndent(),
                listOf(ExternalLabel("do_not_override", mutableMethod.instruction(0)))
            )
        }

        // Remember the selected playback rate.
        OnPlaybackRateItemClickFingerprint.result!!.apply {
            val setPlaybackRateIndex = scanResult.patternScanResult!!.endIndex
            val selectedPlaybackRateRegister =
                (mutableMethod.instruction(setPlaybackRateIndex) as FiveRegisterInstruction).registerD

            mutableMethod.addInstruction(
                setPlaybackRateIndex,
                "invoke-static { v$selectedPlaybackRateRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->rememberPlaybackRate(F)V"
            )
        }


        return PatchResultSuccess()
    }
}
