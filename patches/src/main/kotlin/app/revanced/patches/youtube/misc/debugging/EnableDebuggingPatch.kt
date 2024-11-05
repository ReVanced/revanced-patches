package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/EnableDebuggingPatch;"

@Suppress("unused")
val enableDebuggingPatch = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "misc.debugging.enableDebuggingPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug"),
                    SwitchPreference("revanced_debug_protobuffer"),
                    SwitchPreference("revanced_debug_stacktrace"),
                    SwitchPreference("revanced_debug_toast_on_error"),
                ),
            ),
        )

        // Hook the method that looks up if a feature flag is active or not.
        experimentalFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef,
        ).method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT)

            addInstructions(
                insertIndex,
                """
                    move-result v0
                    invoke-static { p1, p2, v0 }, $EXTENSION_CLASS_DESCRIPTOR->isFeatureFlagEnabled(JZ)Z
                    move-result v0
                    return v0
                """,
            )
        }
    }
}
