package app.revanced.patches.shared.misc.audio

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.findMethodFromToString
import app.revanced.util.indexOfFirstInstructionOrThrow
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
    mainActivityOnCreateFingerprint: Fingerprint,
    subclassExtensionClassDescriptor: String,
    preferenceScreen: BasePreferenceScreen.Screen
) = bytecodePatch(
    name = "Force original audio",
    description = "Adds an option to always use the original audio track.",
) {

    block()

    dependsOn(addResourcesPatch)

    execute {
        addResources("shared", "misc.audio.forceOriginalAudioPatch")

        preferenceScreen.addPreferences(
            SwitchPreference(
                key = "revanced_force_original_audio",
                tag = "app.revanced.extension.shared.settings.preference.ForceOriginalAudioSwitchPreference"
            )
        )

        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static { }, $subclassExtensionClassDescriptor->setEnabled()V"
        )

        // Disable feature flag that ignores the default track flag
        // and instead overrides to the user region language.
        if (fixUseLocalizedAudioTrackFlag()) {
            selectAudioStreamFingerprint.method.insertLiteralOverride(
                AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG,
                "$EXTENSION_CLASS_DESCRIPTOR->ignoreDefaultAudioStream(Z)Z"
            )
        }

        formatStreamModelToStringFingerprint.let {
            val isDefaultAudioTrackMethod = it.originalMethod.findMethodFromToString("isDefaultAudioTrack=")
            val audioTrackDisplayNameMethod = it.originalMethod.findMethodFromToString("audioTrackDisplayName=")
            val audioTrackIdMethod = it.originalMethod.findMethodFromToString("audioTrackId=")

            it.classDef.apply {
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
                        null
                    ).toMutable()
                )

                // Add a helper method because the isDefaultAudioTrack() has only 2 registers and 3 are needed.
                val helperMethodClass = type
                val helperMethodName = "patch_isDefaultAudioTrack"
                val helperMethod = ImmutableMethod(
                    helperMethodClass,
                    helperMethodName,
                    listOf(ImmutableMethodParameter("Z", null, null)),
                    "Z",
                    AccessFlags.PRIVATE.value,
                    null,
                    null,
                    MutableMethodImplementation(6),
                ).toMutable().apply {
                    addInstructionsWithLabels(
                        0,
                        """
                            iget-object v0, p0, $helperMethodClass->$helperFieldName:Ljava/lang/Boolean;
                            if-eqz v0, :call_extension            
                            invoke-virtual { v0 }, Ljava/lang/Boolean;->booleanValue()Z
                            move-result v3
                            return v3
                            
                            :call_extension
                            invoke-virtual { p0 }, $audioTrackIdMethod
                            move-result-object v1
                            
                            invoke-virtual { p0 }, $audioTrackDisplayNameMethod
                            move-result-object v2
        
                            invoke-static { p1, v1, v2 }, $EXTENSION_CLASS_DESCRIPTOR->isDefaultAudioStream(ZLjava/lang/String;Ljava/lang/String;)Z
                            move-result v3
                            
                            invoke-static { v3 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                            move-result-object v0
                            iput-object v0, p0, $helperMethodClass->$helperFieldName:Ljava/lang/Boolean;
                            return v3
                        """
                    )
                }
                methods.add(helperMethod)

                // Modify isDefaultAudioTrack() to call extension helper method.
                isDefaultAudioTrackMethod.apply {
                    val index = indexOfFirstInstructionOrThrow(Opcode.RETURN)
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructions(
                        index,
                        """
                            invoke-direct { p0, v$register }, $helperMethodClass->$helperMethodName(Z)Z
                            move-result v$register
                        """
                    )
                }
            }
        }

        executeBlock()
    }
}
