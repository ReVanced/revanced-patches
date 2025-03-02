package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/EnableDebuggingPatch;"

val enableDebuggingPatch = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
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
            experimentalFeatureFlagParentFingerprint.originalClassDef
        ).method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index,
                    """
                        invoke-static { v$register, p1 }, $EXTENSION_CLASS_DESCRIPTOR->isBooleanFeatureFlagEnabled(ZLjava/lang/Long;)Z
                        move-result v$register
                    """
                )
            }
        }

        experimentalDoubleFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef
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
            experimentalFeatureFlagParentFingerprint.originalClassDef
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

        experimentalStringFeatureFlagFingerprint.match(
            experimentalFeatureFlagParentFingerprint.originalClassDef
        ).method.apply {
            val insertIndex = indexOfFirstInstructionReversedOrThrow(Opcode.MOVE_RESULT_OBJECT)

            addInstructions(
                insertIndex,
                """
                    move-result-object v0
                    invoke-static { v0, p1, p2, p3 }, $EXTENSION_CLASS_DESCRIPTOR->isStringFeatureFlagEnabled(Ljava/lang/String;JLjava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                    return-object v0
                """
            )
        }

        // There exists other experimental accessor methods for byte[]
        // and wrappers for obfuscated classes, but currently none of those are hooked.
    }
}
