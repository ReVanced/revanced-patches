package app.revanced.patches.shared.misc.audio

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableField.Companion.toMutable
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.cloneMutable
import app.revanced.util.findMethodFromToString
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/ForceOriginalAudioPatch;"

/**
 * Patch shared with YouTube and YT Music.
 */
internal fun forceOriginalAudioPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    fixUseLocalizedAudioTrackFlag: BytecodePatchContext.() -> Boolean,
    getMainActivityOnCreateMethod: BytecodePatchContext.() -> MutableMethod,
    subclassExtensionClassDescriptor: String,
    preferenceScreen: BasePreferenceScreen.Screen,
) = bytecodePatch(
    name = "Force original audio",
    description = "Adds an option to always use the original audio track.",
) {
    block()

    dependsOn(addResourcesPatch)

    apply {
        addResources("shared", "misc.audio.forceOriginalAudioPatch")

        preferenceScreen.addPreferences(
            SwitchPreference(
                key = "revanced_force_original_audio",
                tag = "app.revanced.extension.shared.settings.preference.ForceOriginalAudioSwitchPreference",
            ),
        )

        getMainActivityOnCreateMethod().addInstruction(
            0,
            "invoke-static { }, $subclassExtensionClassDescriptor->setEnabled()V",
        )

        // Disable feature flag that ignores the default track flag
        // and instead overrides to the user region language.
        if (fixUseLocalizedAudioTrackFlag()) {
            selectAudioStreamMethodMatch.method.insertLiteralOverride(
                selectAudioStreamMethodMatch[0],
                "$EXTENSION_CLASS_DESCRIPTOR->ignoreDefaultAudioStream(Z)Z",
            )
        }

        val isDefaultAudioTrackMethod =
            formatStreamModelToStringMethodMatch.immutableMethod.findMethodFromToString("isDefaultAudioTrack=")
        val audioTrackDisplayNameMethod =
            formatStreamModelToStringMethodMatch.immutableMethod.findMethodFromToString("audioTrackDisplayName=")
        val audioTrackIdMethod =
            formatStreamModelToStringMethodMatch.immutableMethod.findMethodFromToString("audioTrackId=")

        formatStreamModelToStringMethodMatch.classDef.apply {
            // Add a new field to store the override.
            val helperFieldName = "patch_isDefaultAudioTrackOverride"
            fields.add(
                ImmutableField(
                    type,
                    helperFieldName,
                    "Ljava/lang/Boolean;",
                    // Boolean is a 100% immutable class (all fields are final)
                    // and safe to write to a shared field without volatile/synchronization,
                    // but without volatile the field can show stale data
                    // and the same field is calculated more than once by different threads.
                    AccessFlags.PRIVATE.value or AccessFlags.VOLATILE.value,
                    null,
                    null,
                    null,
                ).toMutable(),
            )

            // Clone the method to add additional registers because the
            // isDefaultAudioTrack() has only 1 or 2 registers and 3 are needed.
            val clonedMethod = isDefaultAudioTrackMethod.cloneMutable(
                additionalRegisters = 4
            )

            // Replace existing method with cloned with more registers.
            methods.apply {
                remove(isDefaultAudioTrackMethod)
                add(clonedMethod)
            }

            clonedMethod.apply {
                // Free registers are added
                val free1 = isDefaultAudioTrackMethod.implementation!!.registerCount + 1
                val free2 = free1 + 1
                val insertIndex = indexOfFirstInstructionReversedOrThrow(Opcode.RETURN)
                val originalResultRegister =
                    getInstruction<OneRegisterInstruction>(insertIndex).registerA

                clonedMethod.addInstructionsAtControlFlowLabel(
                    insertIndex,
                    """
                            iget-object v$free1, p0, $type->$helperFieldName:Ljava/lang/Boolean;
                            if-eqz v$free1, :call_extension            
                            invoke-virtual { v$free1 }, Ljava/lang/Boolean;->booleanValue()Z
                            move-result v$free1
                            return v$free1
                            
                            :call_extension
                            invoke-virtual { p0 }, $audioTrackIdMethod
                            move-result-object v$free1
                            
                            invoke-virtual { p0 }, $audioTrackDisplayNameMethod
                            move-result-object v$free2
        
                            invoke-static { v$originalResultRegister, v$free1, v$free2 }, ${EXTENSION_CLASS_DESCRIPTOR}->isDefaultAudioStream(ZLjava/lang/String;Ljava/lang/String;)Z
                            move-result v$free1
                            
                            invoke-static { v$free1 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                            move-result-object v$free2
                            iput-object v$free2, p0, $type->$helperFieldName:Ljava/lang/Boolean;
                            return v$free1
                        """
                )
            }
        }

        executeBlock()
    }
}
