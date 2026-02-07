package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/TapToSeekPatch;"

val enableTapToSeekPatch = bytecodePatch(
    description = "Adds an option to enable tap to seek on the seekbar of the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    apply {
        addResources("youtube", "interaction.seekbar.enableTapToSeekPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_tap_to_seek"),
        )

        // Find the required methods to tap the seekbar.
        val tapToSeekMethods = onTouchEventHandlerMethodMatch.let {
            fun getReference(index: Int) = it.method.getInstruction<ReferenceInstruction>(index)
                .reference as MethodReference

            listOf(
                getReference(it[0]),
                getReference(it[-1]),
            )
        }

        tapToSeekMethodMatch.let {
            val insertIndex = it[-1] + 1

            it.method.apply {
                val thisInstanceRegister = getInstruction<FiveRegisterInstruction>(
                    insertIndex - 1,
                ).registerC

                val xAxisRegister = this.getInstruction<FiveRegisterInstruction>(
                    it[2],
                ).registerD

                val freeRegister = findFreeRegister(
                    insertIndex,
                    thisInstanceRegister,
                    xAxisRegister,
                )

                val oMethod = tapToSeekMethods[0]
                val nMethod = tapToSeekMethods[1]

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->tapToSeekEnabled()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :disabled
                        invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $oMethod
                        invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $nMethod
                    """,
                    ExternalLabel("disabled", getInstruction(insertIndex)),
                )
            }
        }
    }
}
