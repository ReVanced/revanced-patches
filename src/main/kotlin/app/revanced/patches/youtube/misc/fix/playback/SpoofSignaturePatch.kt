package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.*
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.patches.youtube.video.playerresponse.PlayerResponseMethodHookPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    description = "Spoofs the signature to prevent playback issues.",
    dependencies = [
        SettingsPatch::class,
        PlayerTypeHookPatch::class,
        PlayerResponseMethodHookPatch::class,
        VideoInformationPatch::class,
    ]
)
object SpoofSignaturePatch : BytecodePatch(
    setOf(
        PlayerResponseModelImplFingerprint,
        StoryboardThumbnailParentFingerprint,
        StoryboardRendererSpecFingerprint,
        StoryboardRendererInitFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/spoof/SpoofSignaturePatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                key = "revanced_spoof_signature_verification",
                title = StringResource(
                    "revanced_spoof_signature_verification_title",
                    "Spoof app signature"
                ),
                preferences = listOf(
                    SwitchPreference(
                        "revanced_spoof_signature_verification_enabled",
                        StringResource("revanced_spoof_signature_verification_enabled_title", "Spoof app signature"),
                        StringResource(
                            "revanced_spoof_signature_verification_enabled_summary_on",
                            "App signature spoofed\\n\\n"
                                    + "Side effects include:\\n"
                                    + "• Enhanced bitrate is not available\\n"
                                    + "• Videos cannot be downloaded\\n"
                                    + "• No seekbar thumbnails for paid videos"
                        ),
                        StringResource(
                            "revanced_spoof_signature_verification_enabled_summary_off",
                            "App signature not spoofed\\n\\nVideo playback may not work"
                        ),
                        StringResource(
                            "revanced_spoof_signature_verification_enabled_user_dialog_message",
                            "Turning off this setting will cause video playback issues."
                        )
                    ),
                    SwitchPreference(
                        "revanced_spoof_signature_in_feed_enabled",
                        StringResource("revanced_spoof_signature_in_feed_enabled_title", "Spoof app signature in feed"),
                        StringResource(
                            "revanced_spoof_signature_in_feed_enabled_summary_on",
                            "App signature spoofed\\n\\n"
                                    + "Side effects include:\\n"
                                    + "• Feed videos are missing subtitles\\n"
                                    + "• Automatically played feed videos will show up in your watch history"
                        ),
                        StringResource(
                            "revanced_spoof_signature_in_feed_enabled_summary_off",
                            "App signature not spoofed for feed videos\\n\\n"
                                    + "Feed videos will play for less than 1 minute before encountering playback issues"
                        )
                    )
                )
            )
        )

        // Hook the player parameters.
        PlayerResponseMethodHookPatch += PlayerResponseMethodHookPatch.Hook.ProtoBufferParameter(
            "$INTEGRATIONS_CLASS_DESCRIPTOR->spoofParameter(Ljava/lang/String;)Ljava/lang/String;"
        )

        // Force the seekbar time and chapters to always show up.
        // This is used only if the storyboard spec fetch fails, or when viewing paid videos.
        StoryboardThumbnailParentFingerprint.result?.classDef?.let { classDef ->
            StoryboardThumbnailFingerprint.also {
                it.resolve(
                    context,
                    classDef
                )
            }.result?.let {
                val endIndex = it.scanResult.patternScanResult!!.endIndex
                // Replace existing instruction to preserve control flow label.
                // The replaced return instruction always returns false
                // (it is the 'no thumbnails found' control path),
                // so there is no need to pass the existing return value to integrations.
                it.mutableMethod.replaceInstruction(
                    endIndex,
                    """
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getSeekbarThumbnailOverrideValue()Z
                    """
                )
                // Since this is end of the method must replace one line then add the rest.
                it.mutableMethod.addInstructions(
                    endIndex + 1,
                    """
                    move-result v0
                    return v0
                """
                )
            } ?: throw StoryboardThumbnailFingerprint.exception
        }

        /**
         * Hook StoryBoard renderer url
         */
        PlayerResponseModelImplFingerprint.result?.let {
            it.mutableMethod.apply {
                val getStoryBoardIndex = it.scanResult.patternScanResult!!.endIndex
                val getStoryBoardRegister = getInstruction<OneRegisterInstruction>(getStoryBoardIndex).registerA

                addInstructions(
                    getStoryBoardIndex,
                    """
                        invoke-static { v$getStoryBoardRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$getStoryBoardRegister
                    """
                )
            }
        } ?: throw PlayerResponseModelImplFingerprint.exception

        StoryboardRendererSpecFingerprint.result?.let {
            it.mutableMethod.apply {
                val storyBoardUrlParams = 0

                addInstructionsWithLabels(
                    0,
                    """
                        if-nez p$storyBoardUrlParams, :ignore
                        invoke-static { p$storyBoardUrlParams }, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p$storyBoardUrlParams
                    """,
                    ExternalLabel("ignore", getInstruction(0))
                )
            }
        } ?: throw StoryboardRendererSpecFingerprint.exception

        // Hook recommended value
        StoryboardRendererInitFingerprint.result?.let {
            val moveOriginalRecommendedValueIndex = it.scanResult.patternScanResult!!.endIndex

            val originalValueRegister = it.mutableMethod
                .getInstruction<OneRegisterInstruction>(moveOriginalRecommendedValueIndex).registerA

            it.mutableMethod.apply {
                addInstructions(
                    moveOriginalRecommendedValueIndex + 1,
                    """
                            invoke-static { v$originalValueRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getRecommendedLevel(I)I
                            move-result v$originalValueRegister
                        """
                )
            }
        } ?: throw StoryboardRendererInitFingerprint.exception
    }
}
