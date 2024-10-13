package app.revanced.patches.youtube.misc.autorepeat

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.autoRepeatFingerprint
import app.revanced.patches.youtube.shared.autoRepeatParentFingerprint
import app.revanced.util.matchOrThrow

@Suppress("unused")
val autoRepeatPatch = bytecodePatch(
    name = "Always repeat",
    description = "Adds an option to always repeat videos when they end.",
) {
    dependsOn(
        sharedExtensionPatch,
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

    val autoRepeatParentMatch by autoRepeatParentFingerprint()

    execute { context ->
        addResources("youtube", "misc.autorepeat.autoRepeatPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_auto_repeat"),
        )

        autoRepeatFingerprint.apply {
            match(context, autoRepeatParentMatch.classDef)
        }.matchOrThrow().mutableMethod.apply {
            val playMethod = autoRepeatParentMatch.mutableMethod
            val index = instructions.lastIndex

            // Remove return-void.
            removeInstruction(index)
            // Add own instructions there.
            addInstructionsWithLabels(
                index,
                """
                    invoke-static {}, Lapp/revanced/extension/youtube/patches/AutoRepeatPatch;->shouldAutoRepeat()Z
                    move-result v0
                    if-eqz v0, :noautorepeat
                    invoke-virtual { p0 }, $playMethod
                    :noautorepeat
                    return-void
                """,
            )
        }
    }
}
