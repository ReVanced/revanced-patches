package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.accessFlags
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.method
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.returnType
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/EnableDebuggingPatch;"

/**
 * Patch shared with YouTube and YT Music.
 */
internal fun enableDebuggingPatch(
    sharedExtensionPatch: Patch,
    settingsPatch: Patch,
    vararg compatibleWithPackages: Pair<String, Set<String>>,
    hookStringFeatureFlag: Boolean,
    preferenceScreen: BasePreferenceScreen.Screen,
) = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging and exporting ReVanced logs to the clipboard.",
) {
    compatibleWith(packages = compatibleWithPackages)

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        resourcePatch {
            apply {
                copyResources(
                    "settings",
                    ResourceGroup(
                        "drawable",
                        // Action buttons.
                        "revanced_settings_copy_all.xml",
                        "revanced_settings_deselect_all.xml",
                        "revanced_settings_select_all.xml",
                        // Move buttons.
                        "revanced_settings_arrow_left_double.xml",
                        "revanced_settings_arrow_left_one.xml",
                        "revanced_settings_arrow_right_double.xml",
                        "revanced_settings_arrow_right_one.xml",
                    ),
                )
            }
        },
    )

    apply {
        addResources("shared", "misc.debugging.enableDebuggingPatch")

        val preferences = setOf(
            SwitchPreference("revanced_debug"),
            SwitchPreference("revanced_debug_protobuffer"),
            SwitchPreference("revanced_debug_stacktrace"),
            SwitchPreference("revanced_debug_toast_on_error"),
            NonInteractivePreference(
                "revanced_debug_export_logs_to_clipboard",
                tag = "app.revanced.extension.shared.settings.preference.ExportLogToClipboardPreference",
                selectable = true,
            ),
            NonInteractivePreference(
                "revanced_debug_logs_clear_buffer",
                tag = "app.revanced.extension.shared.settings.preference.ClearLogBufferPreference",
                selectable = true,
            ),
            NonInteractivePreference(
                "revanced_debug_feature_flags_manager",
                tag = "app.revanced.extension.shared.settings.preference.FeatureFlagsManagerPreference",
                selectable = true,
            ),
        )

        preferenceScreen.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = preferences,
            ),
        )

        val experimentalBooleanFeatureFlagMethodMatch =
            experimentalFeatureFlagUtilMethod.immutableClassDef.experimentalBooleanFeatureFlagMethodMatch

        experimentalBooleanFeatureFlagMethodMatch.let {
            it.method.apply {
                // In some versions, freeRegister is not available.
                // The easiest workaround is to copy the method to minimize modifications to the instructions.
                val helperMethodName = "patch_getBooleanFeatureFlag"

                // Copy the method.
                val helperMethod = cloneMutable(name = helperMethodName)

                // Add the method.
                it.classDef.methods.add(helperMethod)

                addInstructions(
                    0,
                    """
                        # Invoke the copied method (helper method).
                        invoke-static {p0, p1, p2, p3}, $helperMethod
                        move-result p0
                        
                        # Convert the flag value to 'Long' format to pass it to the extension.
                        invoke-static {p1, p2}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                        move-result-object p1
                        
                        # Redefine boolean in the extension.
                        invoke-static {p0, p1}, ${EXTENSION_CLASS_DESCRIPTOR}->isBooleanFeatureFlagEnabled(ZLjava/lang/Long;)Z
                        move-result p0
                        
                        # Since the copied method (helper method) has already been invoked, it just returns.
                        return p0
                    """
                )
            }
        }

        // In some versions, the classes for 'experimentalBooleanFeatureFlagMethod' and
        // 'experimentalDoubleFeatureFlagMethod, experimentalLongFeatureFlagMethod, experimentalStringFeatureFlagMethod'
        // are different.
        // To handle this, rely on a parent methods.
        val experimentalFeatureFlagParentMethod = firstMethodDeclaratively {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returnType("Z")
            parameterTypes("J", "Z")
            instructions(method { this == experimentalBooleanFeatureFlagMethodMatch.method })
        }

        experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalDoubleFeatureFlagMethod()
            .apply {
                val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT_WIDE)

                addInstructions(
                    insertIndex,
                    """
                    move-result-wide v0 # Also clobbers v1 (p0) since result is wide.
                    invoke-static/range { v0 .. v5 }, $EXTENSION_CLASS_DESCRIPTOR->isDoubleFeatureFlagEnabled(DJD)D
                    move-result-wide v0
                    return-wide v0
                """,
                )
            }

        experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalLongFeatureFlagMethod()
            .apply {
                val insertIndex = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT_WIDE)

                addInstructions(
                    insertIndex,
                    """
                    move-result-wide v0
                    invoke-static/range { v0 .. v5 }, $EXTENSION_CLASS_DESCRIPTOR->isLongFeatureFlagEnabled(JJJ)J
                    move-result-wide v0
                    return-wide v0
                """,
                )
            }

        if (hookStringFeatureFlag) {
            experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalStringFeatureFlagMethod()
                .apply {
                    val insertIndex =
                        indexOfFirstInstructionReversedOrThrow(Opcode.MOVE_RESULT_OBJECT)

                    addInstructions(
                        insertIndex,
                        """
                        move-result-object v0
                        invoke-static { v0, p1, p2, p3 }, $EXTENSION_CLASS_DESCRIPTOR->isStringFeatureFlagEnabled(Ljava/lang/String;JLjava/lang/String;)Ljava/lang/String;
                        move-result-object v0
                        return-object v0
                    """,
                    )
                }
        }

        // There exists other experimental accessor methods for byte[]
        // and wrappers for obfuscated classes, but currently none of those are hooked.
    }
}
