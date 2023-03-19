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
import app.revanced.patches.youtube.misc.video.speed.remember.annotation.RememberPlaybackSpeedCompatibility
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.ChangePlaybackSpeedFragmentStateFingerprint
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.InitializePlaybackSpeedValuesFingerprint
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.OnPlaybackSpeedItemClickFingerprint
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@Patch
@Name("remember-playback-speed")
@Description("Adds the ability to remember the playback speed you chose in the video playback speed flyout.")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, VideoIdPatch::class])
@RememberPlaybackSpeedCompatibility
@Version("0.0.1")
class RememberPlaybackSpeedPatch : BytecodePatch(
    listOf(ChangePlaybackSpeedFragmentStateFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_playback_speed_last_selected",
                StringResource(
                    "revanced_remember_playback_speed_last_selected_title",
                    "Remember playback speed changes"
                ),
                true,
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_on",
                    "Playback speed changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_off",
                    "Playback speed changes only apply to the current video"
                )
            )
        )

        context.resolveFingerprints()

        VideoIdPatch.injectCall("${INTEGRATIONS_CLASS_DESCRIPTOR}->newVideoLoaded(Ljava/lang/String;)V")

        // Set the remembered playback speed.
        InitializePlaybackSpeedValuesFingerprint.result!!.apply {
            // Infer everything necessary for setPlaybackSate()

            val playbackHandlerWrapperFieldReference =
                (object : MethodFingerprint(opcodes = listOf(Opcode.IF_EQZ)) {}).apply {
                    OnPlaybackSpeedItemClickFingerprint.result!!.apply {
                        resolve(
                            context,
                            method,
                            classDef
                        )
                    }
                }.getReference(-1)
            val playbackHandlerWrapperImplementorClassReference = OnPlaybackSpeedItemClickFingerprint
                .getReference(-1)
            val playbackHandlerFieldReference = OnPlaybackSpeedItemClickFingerprint
                .getReference()
            val setPlaybackSpeedMethodReference = OnPlaybackSpeedItemClickFingerprint
                .getReference(1)

            mutableMethod.addInstructions(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getCurrentPlaybackSpeed()F
                    move-result v0
                    # check if the playback speed is not 1.0x
                    const/high16 v1, 0x3f800000  # 1.0f
                    cmpg-float v1, v0, v1
                    if-eqz v1, :do_not_override

                    # invoke setPlaybackSpeed
                    iget-object v1, p0, $playbackHandlerWrapperFieldReference 
                    check-cast v1, $playbackHandlerWrapperImplementorClassReference
                    iget-object v2, v1, $playbackHandlerFieldReference
                    invoke-virtual {v2, v0}, $setPlaybackSpeedMethodReference
                """.trimIndent(),
                listOf(ExternalLabel("do_not_override", mutableMethod.instruction(0)))
            )
        }

        // Remember the selected playback speed.
        OnPlaybackSpeedItemClickFingerprint.result!!.apply {
            val setPlaybackSpeedIndex = scanResult.patternScanResult!!.endIndex
            val selectedPlaybackSpeedRegister =
                (mutableMethod.instruction(setPlaybackSpeedIndex) as FiveRegisterInstruction).registerD

            mutableMethod.addInstruction(
                setPlaybackSpeedIndex,
                "invoke-static { v$selectedPlaybackSpeedRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlaybackSpeed(F)V"
            )
        }


        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/speed/RememberPlaybackSpeedPatch;"

        fun MethodFingerprint.getReference(offsetFromPatternScanResultStartIndex: Int = 0) = this.result!!.let {
            val referenceInstruction = it.mutableMethod
                .instruction(it.scanResult.patternScanResult!!.startIndex + offsetFromPatternScanResultStartIndex) as ReferenceInstruction
            referenceInstruction.reference.toString()
        }

        fun BytecodeContext.resolveFingerprints() {
            ChangePlaybackSpeedFragmentStateFingerprint.result?.also {
                fun MethodFingerprint.resolve() = resolve(this@resolveFingerprints, it.classDef)

                OnPlaybackSpeedItemClickFingerprint.resolve()
                InitializePlaybackSpeedValuesFingerprint.resolve()

            } ?: throw ChangePlaybackSpeedFragmentStateFingerprint.toErrorResult()
        }
    }
}
