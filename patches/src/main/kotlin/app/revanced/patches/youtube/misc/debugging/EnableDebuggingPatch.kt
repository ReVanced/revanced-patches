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

        // Hook the methods that look up if a feature flag is active.
        experimentalBooleanFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef()
        ).method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT)

            // It appears that all usage of this method has a default of 'false',
            // so there's no need to pass in the default.
            addInstructions(
                insertIndex,
                """
                    move-result v0
                    invoke-static { v0, p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->isBooleanFeatureFlagEnabled(ZJ)Z
                    move-result v0
                    return v0
                """
            )
        }

        experimentalDoubleFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef()
        ).method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT_WIDE)

            addInstructions(
                insertIndex,
                """
                    move-result-wide v0     # Also clobbers v1 (p0) since result is wide.
                    invoke-static/range { v0 .. v5 }, $EXTENSION_CLASS_DESCRIPTOR->isDoubleFeatureFlagEnabled(DJD)D
                    move-result-wide v0
                    return-wide v0
                """
            )
        }

        experimentalLongFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef()
        ).method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT_WIDE)

            addInstructions(
                insertIndex,
                """
                    move-result-wide v0
                    invoke-static/range { v0 .. v5 }, $EXTENSION_CLASS_DESCRIPTOR->isLongFeatureFlagEnabled(JJJ)J
                    move-result-wide v0
                    return-wide v0
                """
            )
        }

        // There exists other experimental accessor methods for String, byte[], and wrappers for obfuscated classes,
        // but currently none of those are hooked.
    }
}
