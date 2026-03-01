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
            SwitchPreference("revanced_debug_protocolbuffer"),
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
                // Not enough registers in the method. Clone the method and use the
                // original method as an intermediate to call extension code.


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
                        
                        # Redefine boolean in the extension.
                        invoke-static { p0, p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->isBooleanFeatureFlagEnabled(ZJ)Z
                        move-result p0
                        
                        # Since the copied method (helper method) has already been invoked, it just returns.
                        return p0
                    """
                )
            }
        }

        if (hookDoubleFeatureFlag())
        // 21.06+ doesn't have enough registers and needs to also clone.
            experimentalFeatureFlagUtilMethod.immutableClassDef.getExperimentalDoubleFeatureFlagMethod()
                .cloneMutableAndPreserveParameters().apply {
                    val helperMethod = cloneMutable(name = "patch_getDoubleFeatureFlag")

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
            experimentalFeatureFlagUtilMethod.immutableClassDef.getExperimentalLongFeatureFlagMethod()
                .cloneMutableAndPreserveParameters().apply {
                    val helperMethod = cloneMutable(name = "patch_getLongFeatureFlag")

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
            experimentalFeatureFlagUtilMethod.immutableClassDef.getExperimentalStringFeatureFlagMethod()
                .apply {
                    val helperMethod = cloneMutable(name = "patch_getStringFeatureFlag")

                    classDef.methods.add(helperMethod)

                    addInstructions(
                        0,
                        """
                        invoke-static { p0, p1, p2, p3 }, $helperMethod
                        move-result-object p0
                        
                        invoke-static { p0, p1, p2, p3 }, ${EXTENSION_CLASS_DESCRIPTOR}->isStringFeatureFlagEnabled(Ljava/lang/String;JLjava/lang/String;)Ljava/lang/String;
                        move-result-object p0
                        
                        return-object p0
                    """
                    )
                }

        // There exists other experimental accessor methods for byte[]
        // and wrappers for obfuscated classes, but currently none of those are hooked.
    }
}
