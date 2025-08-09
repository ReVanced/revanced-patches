package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.videoQualityChangedFingerprint
import app.revanced.patches.youtube.video.information.onCreateHook
import app.revanced.patches.youtube.video.information.videoInformationPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/RememberVideoQualityPatch;"

val rememberVideoQualityPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        videoInformationPatch,
        playerTypeHookPatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.quality.rememberVideoQualityPatch")

        settingsMenuVideoQualityGroup.addAll(listOf(
            ListPreference(
                key = "revanced_video_quality_default_mobile",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            ListPreference(
                key = "revanced_video_quality_default_wifi",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_video_quality_last_selected"),

            ListPreference(
                key = "revanced_shorts_quality_default_mobile",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values",
            ),
            ListPreference(
                key = "revanced_shorts_quality_default_wifi",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_shorts_quality_last_selected"),
            SwitchPreference("revanced_remember_video_quality_last_selected_toast")
        ))

        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "newVideoStarted")

        // Inject a call to remember the selected quality for Shorts.
        videoQualityItemOnClickFingerprint.match(
            videoQualityItemOnClickParentFingerprint.classDef
        ).method.addInstruction(
            0,
            "invoke-static { p3 }, $EXTENSION_CLASS_DESCRIPTOR->userChangedShortsQuality(I)V"
        )

        // Inject a call to remember the user selected quality for regular videos.
        videoQualityChangedFingerprint.let {
            it.method.apply {
                val index = it.patternMatch!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->userChangedQuality(I)V",
                )
            }
        }
    }
}
