package app.revanced.patches.youtube.layout.hide.rollingnumber

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.rollingNumberTextViewAnimationUpdateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableRollingNumberAnimationsPatch;"

val disableRollingNumberAnimationPatch = bytecodePatch(
    name = "Disable rolling number animations",
    description = "Adds an option to disable rolling number animations of video view count, user likes, and upload time.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.hide.rollingnumber.disableRollingNumberAnimationPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_rolling_number_animations"),
        )

        // Animations are disabled by preventing an Image from being applied to the text span,
        // which prevents the animations from appearing.

        val patternMatch = rollingNumberTextViewAnimationUpdateFingerprint.patternMatch!!
        val blockStartIndex = patternMatch.startIndex
        val blockEndIndex = patternMatch.endIndex + 1
        rollingNumberTextViewAnimationUpdateFingerprint.method.apply {
            val freeRegister = getInstruction<OneRegisterInstruction>(blockStartIndex).registerA

            // ReturnYouTubeDislike also makes changes to this same method,
            // and must add control flow label to a noop instruction to
            // ensure RYD patch adds its changes after the control flow label.
            addInstructions(blockEndIndex, "nop")

            addInstructionsWithLabels(
                blockStartIndex,
                """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->disableRollingNumberAnimations()Z
                        move-result v$freeRegister
                        if-nez v$freeRegister, :disable_animations
                    """,
                ExternalLabel("disable_animations", getInstruction(blockEndIndex)),
            )
        }
    }
}
