package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.video.hdr.fingerprints.HdrCapabilitiesFingerprint
import app.revanced.util.exception

@Patch(
    name = "Disable HDR video",
    description = "Adds options to disable HDR video.",
    dependencies = [SettingsPatch::class],
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
object DisableHdrVideoPatch : BytecodePatch(
    setOf(HdrCapabilitiesFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        HdrCapabilitiesFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                0, """
                    invoke-static {}, $VIDEO_PATH/HDRVideoPatch;->disableHDRVideo()Z
                    move-result v0
                    if-nez v0, :default
                    return v0
                    """, ExternalLabel("default", getInstruction(0))
            )
        } ?: throw HdrCapabilitiesFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: VIDEO_SETTINGS",
                "SETTINGS: DISABLE_HDR_VIDEO"
            )
        )

        SettingsPatch.updatePatchStatus("Disable HDR video")

    }
}
