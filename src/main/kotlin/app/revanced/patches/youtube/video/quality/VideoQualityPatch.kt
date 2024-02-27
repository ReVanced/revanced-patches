package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.fingerprints.NewVideoQualityChangedFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.overridequality.OverrideQualityHookPatch
import app.revanced.patches.youtube.utils.overridespeed.OverrideSpeedHookPatch
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.patches.youtube.utils.videoid.withoutshorts.VideoIdWithoutShortsPatch
import app.revanced.patches.youtube.video.quality.fingerprints.VideoQualitySetterFingerprint
import app.revanced.util.copyXmlNode
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Default video quality",
    description = "Adds an option to set the default video quality.",
    dependencies = [
        OverrideQualityHookPatch::class,
        OverrideSpeedHookPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class,
        VideoIdWithoutShortsPatch::class
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
object VideoQualityPatch : BytecodePatch(
    setOf(
        NewVideoQualityChangedFingerprint,
        VideoQualitySetterFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        NewVideoQualityChangedFingerprint.result?.let {
            it.mutableMethod.apply {
                val index = it.scanResult.patternScanResult!!.startIndex
                val qualityRegister = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static {v$qualityRegister}, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->userChangedQuality(I)V"
                )

            }
        } ?: throw NewVideoQualityChangedFingerprint.exception

        VideoQualitySetterFingerprint.result?.let {
            val onItemClickMethod =
                it.mutableClass.methods.find { method -> method.name == "onItemClick" }

            onItemClickMethod?.apply {
                val listItemIndexParameter = 3

                addInstruction(
                    0,
                    "invoke-static {p$listItemIndexParameter}, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->userChangedQualityIndex(I)V"
                )
            } ?: throw PatchException("Failed to find onItemClick method")
        } ?: throw VideoQualitySetterFingerprint.exception
        VideoIdWithoutShortsPatch.injectCall("$INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->initialize(Ljava/lang/String;)V")
        VideoIdWithoutShortsPatch.injectCall("$INTEGRATIONS_RELOAD_VIDEO_CLASS_DESCRIPTOR->setVideoId(Ljava/lang/String;)V")

        /**
         * Copy arrays
         */
        contexts.copyXmlNode("youtube/quality/host", "values/arrays.xml", "resources")


        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: VIDEO_SETTINGS",
                "SETTINGS: VIDEO_EXPERIMENTAL_FLAGS",
                "SETTINGS: DEFAULT_VIDEO_QUALITY"
            )
        )

        SettingsPatch.updatePatchStatus("Default video quality")

    }

    private const val INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/VideoQualityPatch;"
    private const val INTEGRATIONS_RELOAD_VIDEO_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/ReloadVideoPatch;"
}