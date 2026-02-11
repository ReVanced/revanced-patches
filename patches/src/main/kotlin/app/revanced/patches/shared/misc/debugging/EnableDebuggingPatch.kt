package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.accessFlags
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.method
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchException
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
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    hookStringFeatureFlag: BytecodePatchBuilder.() -> Boolean,
    hookLongFeatureFlag: BytecodePatchBuilder.() -> Boolean,
    hookDoubleFeatureFlag: BytecodePatchBuilder.() -> Boolean,
    preferenceScreen: BasePreferenceScreen.Screen,
    additionalDebugPreferences: List<BasePreference> = emptyList()
) = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging and exporting ReVanced logs to the clipboard.",
) {

    dependsOn(
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
                        "revanced_settings_arrow_right_one.xml"
                    )
                )
            }
        }
    )

    block()

    apply {
        executeBlock()

        addResources("shared", "misc.debugging.enableDebuggingPatch")

        val preferences = mutableSetOf<BasePreference>(
            SwitchPreference("revanced_debug"),
        )

        preferences + additionalDebugPreferences

        preferences += listOf(
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

        preferenceScreen.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = preferences,
            )
        )

        val experimentalBooleanFeatureFlagMethodMatch =
            experimentalFeatureFlagUtilMethod.immutableClassDef.experimentalBooleanFeatureFlagMethodMatch

        experimentalBooleanFeatureFlagMethodMatch.let {
            it.method.apply {
                // In some versions, freeRegister is not available.
                // The easiest workaround is to copy the method to minimize modifications to the instructions.

                // Copy the method.
                val helperMethod = cloneMutable(name = "patch_getBooleanFeatureFlag")

                // Add the method.
                it.classDef.methods.add(helperMethod)

                addInstructions(
                    0,
                    """
                        # Invoke the copied method (helper method).
                        invoke-static { p0, p1, p2, p3 }, $helperMethod
                        move-result p0
                        
                        # Convert the flag value to 'Long' format to pass it to the extension.
                        invoke-static { p1, p2 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                        move-result-object p1
                        
                        # Redefine boolean in the extension.
                        invoke-static { p0, p1 }, ${EXTENSION_CLASS_DESCRIPTOR}->isBooleanFeatureFlagEnabled(ZLjava/lang/Long;)Z
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

        if (hookDoubleFeatureFlag())
            experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalDoubleFeatureFlagMethod()
                .apply {
                    // In some versions, freeRegister is not available.
                    // The easiest workaround is to copy the method to minimize modifications to the instructions.
                    if (implementation!!.registerCount < 8) {
                        throw PatchException("Target method has less than 8 registers")
                    }

                    // Copy the method.
                    val helperMethod = cloneMutable(name = "patch_getDoubleFeatureFlag")

                    // Add the method.
                    classDef.methods.add(helperMethod)

                    addInstructions(
                        0,
                        """
                        # Invoke the copied method (helper method).
                        invoke-static/range { p0 .. p4 }, $helperMethod
                        move-result-wide v0
                        
                        # Move parameter registers to lower register range to use invoke-static/range.
                        move-wide v2, p1
                        move-wide v4, p3

                        invoke-static/range { v0 .. v5 }, ${EXTENSION_CLASS_DESCRIPTOR}->isDoubleFeatureFlagEnabled(DJD)D
                        move-result-wide v0

                        # Since the copied method (helper method) has already been invoked, it just returns.
                        return-wide v0
                    """
                    )
                }

        if (hookLongFeatureFlag())
            experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalLongFeatureFlagMethod()
                .apply {
                    // In some versions, freeRegister is not available.
                    // The easiest workaround is to copy the method to minimize modifications to the instructions.
                    if (implementation!!.registerCount < 8) {
                        throw PatchException("Target method has less than 8 registers")
                    }

                    // Copy the method.
                    val helperMethod = cloneMutable(name = "patch_getLongFeatureFlag")

                    // Add the method.
                    classDef.methods.add(helperMethod)

                    addInstructions(
                        0,
                        """
                        # Invoke the copied method (helper method).
                        invoke-static/range { p0 .. p4 }, $helperMethod
                        move-result-wide v0
                        
                        # Move parameter registers to lower register range to use invoke-static/range.
                        move-wide v2, p1
                        move-wide v4, p3

                        invoke-static/range { v0 .. v5 }, ${EXTENSION_CLASS_DESCRIPTOR}->isLongFeatureFlagEnabled(JJJ)J
                        move-result-wide v0

                        # Since the copied method (helper method) has already been invoked, it just returns.
                        return-wide v0
                    """
                    )
                }

        if (hookStringFeatureFlag())
            experimentalFeatureFlagParentMethod.immutableClassDef.getExperimentalStringFeatureFlagMethod()
                .apply {
                    val insertIndex =
                        indexOfFirstInstructionReversedOrThrow(Opcode.MOVE_RESULT_OBJECT)

                    addInstructions(
                        insertIndex,
                        """
                    move-result-object v0
                    invoke-static { v0, p1, p2, p3 }, ${EXTENSION_CLASS_DESCRIPTOR}->isStringFeatureFlagEnabled(Ljava/lang/String;JLjava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                    return-object v0
                """
                    )
                }

        // There exists other experimental accessor methods for byte[]
        // and wrappers for obfuscated classes, but currently none of those are hooked.
    }
}
