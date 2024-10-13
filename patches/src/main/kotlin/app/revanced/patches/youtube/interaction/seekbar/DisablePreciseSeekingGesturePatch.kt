package app.revanced.patches.youtube.interaction.seekbar

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
import app.revanced.util.applyMatch

@Suppress("unused")
val disablePreciseSeekingGesturePatch = bytecodePatch(
    name = "Disable precise seeking gesture",
    description = "Adds an option to disable precise seeking when swiping up on the seekbar.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val swipingUpGestureParentMatch by swipingUpGestureParentFingerprint()

    execute { context ->
        addResources("youtube", "interaction.seekbar.disablePreciseSeekingGesturePatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_disable_precise_seeking_gesture"),
        )
        val extensionMethodDescriptor =
            "Lapp/revanced/extension/youtube/patches/DisablePreciseSeekingGesturePatch;"

        allowSwipingUpGestureFingerprint.applyMatch(
            context,
            swipingUpGestureParentMatch
        ).mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $extensionMethodDescriptor->isGestureDisabled()Z
                    move-result v0
                    if-eqz v0, :disabled
                    return-void
                """,
                ExternalLabel("disabled", getInstruction(0))
            )
        }

        showSwipingUpGuideFingerprint.applyMatch(
            context,
            swipingUpGestureParentMatch
        ).mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $extensionMethodDescriptor->isGestureDisabled()Z
                    move-result v0
                    if-eqz v0, :disabled
                    const/4 v0, 0x0
                    return v0
                """,
                ExternalLabel("disabled", getInstruction(0))
            )
        }
    }
}
