package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.fingerprints.NewVideoQualityChangedFingerprint
import app.revanced.patches.youtube.utils.fingerprints.VideoEndFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.overridespeed.OverrideSpeedHookPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.videocpn.VideoCpnPatch
import app.revanced.patches.youtube.video.speed.fingerprints.PlaybackSpeedInitializeFingerprint
import app.revanced.patches.youtube.video.speed.fingerprints.NewPlaybackSpeedChangedFingerprint
import app.revanced.util.exception
import app.revanced.util.updatePatchStatus
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Default playback speed",
    description = "Adds an option to set the default playback speed.",
    dependencies = [
        OverrideSpeedHookPatch::class,
        SettingsPatch::class,
        VideoCpnPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(
    setOf(
        NewVideoQualityChangedFingerprint,
        VideoEndFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        NewVideoQualityChangedFingerprint.result?.let { parentResult ->
            NewPlaybackSpeedChangedFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let { result ->
                arrayOf(result, OverrideSpeedHookPatch.playbackSpeedChangedResult).forEach {
                    it.mutableMethod.apply {
                        val index = it.scanResult.patternScanResult!!.endIndex
                        val register = getInstruction<FiveRegisterInstruction>(index).registerD

                        addInstruction(
                            index,
                            "invoke-static {v$register}, $INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR->userChangedSpeed(F)V"
                        )
                    }
                }
            } ?: throw NewPlaybackSpeedChangedFingerprint.exception
        } ?: throw NewVideoQualityChangedFingerprint.exception

        VideoEndFingerprint.result?.let { parentResult ->
            PlaybackSpeedInitializeFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.endIndex
                    val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                    addInstructions(
                        insertIndex, """
                            invoke-static {v$insertRegister}, $INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR->getPlaybackSpeedInShorts(F)F
                            move-result v$insertRegister
                            """
                    )
                }
            } ?: throw PlaybackSpeedInitializeFingerprint.exception
        } ?: throw VideoEndFingerprint.exception

        VideoCpnPatch.injectCall("$INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR->newVideoStarted(Ljava/lang/String;)V")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: VIDEO_SETTINGS",
                "SETTINGS: VIDEO_EXPERIMENTAL_FLAGS",
                "SETTINGS: DEFAULT_PLAYBACK_SPEED"
            )
        )

        SettingsPatch.updatePatchStatus("Default playback speed")

        context.updatePatchStatus("$UTILS_PATH/PatchStatus;", "DefaultPlaybackSpeed")

    }

    private const val INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/PlaybackSpeedPatch;"
}