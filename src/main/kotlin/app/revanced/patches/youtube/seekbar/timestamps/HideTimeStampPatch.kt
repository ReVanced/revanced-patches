package app.revanced.patches.youtube.seekbar.timestamps

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.seekbar.timestamps.fingerprints.TimeCounterFingerprint
import app.revanced.patches.youtube.utils.fingerprints.PlayerSeekbarColorFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SEEKBAR
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Hide time stamp",
    description = "Adds an option to hide the timestamp in the bottom left of the video player.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class,
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
object HideTimeStampPatch : BytecodePatch(
    setOf(PlayerSeekbarColorFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        PlayerSeekbarColorFingerprint.result?.let { parentResult ->
            TimeCounterFingerprint.also { it.resolve(context, parentResult.classDef) }.result?.let {
                it.mutableMethod.apply {
                    addInstructionsWithLabels(
                        0, """
                        invoke-static {}, $SEEKBAR->hideTimeStamp()Z
                        move-result v0
                        if-eqz v0, :show_time_stamp
                        return-void
                        """, ExternalLabel("show_time_stamp", getInstruction(0))
                    )
                }
            } ?: throw TimeCounterFingerprint.exception
        } ?: throw PlayerSeekbarColorFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SEEKBAR_SETTINGS",
                "SETTINGS: HIDE_TIME_STAMP"
            )
        )

        SettingsPatch.updatePatchStatus("Hide time stamp")

    }
}
