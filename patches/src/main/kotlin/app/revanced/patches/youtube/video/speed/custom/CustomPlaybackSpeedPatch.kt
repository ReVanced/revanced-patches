package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.speed.settingsMenuVideoSpeedGroup
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstruction
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/PlaybackSpeedMenuFilterPatch;"

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/speed/CustomPlaybackSpeedPatch;"

internal val customPlaybackSpeedPatch = bytecodePatch(
    description = "Adds custom playback speed options.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        lithoFilterPatch,
        versionCheckPatch,
        recyclerViewTreeHookPatch
    )

    execute {
        addResources("youtube", "video.speed.custom.customPlaybackSpeedPatch")

        settingsMenuVideoSpeedGroup.addAll(
            listOf(
                SwitchPreference("revanced_custom_speed_menu"),
                TextPreference(
                    "revanced_custom_playback_speeds",
                    inputType = InputType.TEXT_MULTI_LINE
                ),
            )
        )

        if (is_19_25_or_greater) {
            settingsMenuVideoSpeedGroup.add(
                TextPreference("revanced_speed_tap_and_hold", inputType = InputType.NUMBER_DECIMAL),
            )
        }

        // Override the min/max speeds that can be used.
        speedLimiterFingerprint.method.apply {
            val limitMinIndex = indexOfFirstLiteralInstructionOrThrow(0.25f)
            var limitMaxIndex = indexOfFirstLiteralInstruction(2.0f)
            // Newer targets have 4x max speed.
            if (limitMaxIndex < 0) {
                limitMaxIndex = indexOfFirstLiteralInstructionOrThrow(4.0f)
            }

            val limitMinRegister = getInstruction<OneRegisterInstruction>(limitMinIndex).registerA
            val limitMaxRegister = getInstruction<OneRegisterInstruction>(limitMaxIndex).registerA

            replaceInstruction(limitMinIndex, "const/high16 v$limitMinRegister, 0.0f")
            replaceInstruction(limitMaxIndex, "const/high16 v$limitMaxRegister, 8.0f")
        }

        // Close the unpatched playback dialog and show the modern custom dialog.
        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion


        // region Custom tap and hold 2x speed.

        if (is_19_25_or_greater) {
            disableFastForwardNoticeFingerprint.method.apply {
                val index = indexOfFirstInstructionOrThrow {
                    (this as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits()
                }
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->tapAndHoldSpeed()F
                        move-result v$register
                    """
                )
            }
        }

        // endregion
    }
}
