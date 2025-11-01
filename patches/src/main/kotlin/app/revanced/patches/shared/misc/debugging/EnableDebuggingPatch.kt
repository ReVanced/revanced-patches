package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/EnableDebuggingPatch;"

/**
 * Patch shared with YouTube and YT Music.
 */
internal fun enableDebuggingPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    hookStringFeatureFlag: Boolean,
    preferenceScreen: BasePreferenceScreen.Screen,
    additionalDebugPreferences: List<BasePreference> = emptyList()
) = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging and exporting ReVanced logs to the clipboard.",
) {

    dependsOn(
        addResourcesPatch,
        resourcePatch {
            execute {
                copyResources(
                    "settings",
                    ResourceGroup("drawable",
                        // Action buttons.
                        "revanced_settings_copy_all.xml",
                        "revanced_settings_deselect_all.xml",
                        "revanced_settings_select_all.xml",
                        // Move buttons.
                        "revanced_settings_arrow_left_double.xml",
                        "revanced_settings_arrow_left_one.xml",
                        "revanced_settings_arrow_right_double.xml",
                        "revanced_settings_arrow_right_one.xml"
                    )
                )
            }
        }
    )

    block()

    execute {
        executeBlock()

        addResources("shared", "misc.debugging.enableDebuggingPatch")

        val preferences = mutableSetOf<BasePreference>(
            SwitchPreference("revanced_debug"),
        )

        preferences.addAll(additionalDebugPreferences)

        preferences.addAll(
            listOf(
                SwitchPreference("revanced_debug_stacktrace"),
                SwitchPreference("revanced_debug_toast_on_error"),
                NonInteractivePreference(
                    "revanced_debug_export_logs_to_clipboard",
                    tag = "app.revanced.extension.shared.settings.preference.ExportLogToClipboardPreference",
                    selectable = true
                ),
                NonInteractivePreference(
                    "revanced_debug_logs_clear_buffer",
                    tag = "app.revanced.extension.shared.settings.preference.ClearLogBufferPreference",
                    selectable = true
                ),
                NonInteractivePreference(
                    "revanced_debug_feature_flags_manager",
                    tag = "app.revanced.extension.shared.settings.preference.FeatureFlagsManagerPreference",
                    selectable = true
                )
            )
        )

        preferenceScreen.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = preferences,
            )
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

        if (hookStringFeatureFlag) experimentalStringFeatureFlagFingerprint.match(
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
