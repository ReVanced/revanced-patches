package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_09_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ShortsAutoplayPatch;"

val shortsAutoplayPatch = bytecodePatch(
    name = "Shorts autoplay",
    description = "Adds options to automatically play the next Short.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        resourceMappingPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.shortsautoplay.shortsAutoplayPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_shorts_autoplay"),
        )

        if (is_19_34_or_greater) {
            PreferenceScreen.SHORTS.addPreferences(
                SwitchPreference("revanced_shorts_autoplay_background"),
            )
        }

        // Main activity is used to check if app is in pip mode.
        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->setMainActivity(Landroid/app/Activity;)V",
        )

        val reelEnumClass = reelEnumConstructorFingerprint.originalClassDef.type

        reelEnumConstructorFingerprint.method.apply {
            val insertIndex = reelEnumConstructorFingerprint.patternMatch!!.startIndex

            addInstructions(
                insertIndex,
                """
                    # Pass the first enum value to extension.
                    # Any enum value of this type will work.
                    sget-object v0, $reelEnumClass->a:$reelEnumClass
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setYTShortsRepeatEnum(Ljava/lang/Enum;)V
                """,
            )
        }

        reelPlaybackRepeatFingerprint.method.apply {
            // The behavior enums are looked up from an ordinal value to an enum type.
            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == reelEnumClass &&
                    reference.parameterTypes.firstOrNull() == "I" &&
                    reference.returnType == reelEnumClass
            }.forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructions(
                    index + 2,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->changeShortsRepeatBehavior(Ljava/lang/Enum;)Ljava/lang/Enum;
                        move-result-object v$register
                    """,
                )
            }
        }

        // As of YouTube 20.09, Google has removed the code for 'Autoplay' and 'Pause' from this method.
        // Manually restore the removed 'Autoplay' code.
        if (is_20_09_or_greater) {
            // Variable names are only a rough guess of what these methods do.
            val userActionMethodIndex = indexOfInitializationInstruction(reelPlaybackFingerprint.method)
            val userActionMethodReference = reelPlaybackFingerprint.method
                .getInstruction<ReferenceInstruction>(userActionMethodIndex).reference as MethodReference
            val reelSequenceControllerMethodIndex = reelPlaybackFingerprint.method
                .indexOfFirstInstructionOrThrow(userActionMethodIndex, Opcode.INVOKE_VIRTUAL)
            val reelSequenceControllerMethodReference = reelPlaybackFingerprint.method
                .getInstruction<ReferenceInstruction>(reelSequenceControllerMethodIndex).reference as MethodReference

            reelPlaybackRepeatFingerprint.method.apply {
                // Find the first call modified by extension code above.
                val extensionReturnResultIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_STATIC &&
                            getReference<MethodReference>()?.definingClass == EXTENSION_CLASS_DESCRIPTOR
                } + 1
                val enumRegister = getInstruction<OneRegisterInstruction>(extensionReturnResultIndex).registerA
                val getReelSequenceControllerIndex = indexOfFirstInstructionOrThrow(extensionReturnResultIndex) {
                    val reference = getReference<FieldReference>()
                    opcode == Opcode.IGET_OBJECT &&
                            reference?.definingClass == definingClass &&
                            reference.type == reelSequenceControllerMethodReference.definingClass
                }
                val getReelSequenceControllerReference =
                    getInstruction<ReferenceInstruction>(getReelSequenceControllerIndex).reference

                // Add a helper method to avoid finding multiple free registers.
                // If enum is autoplay then method performs autoplay and returns null,
                // otherwise returns the same enum.
                val helperClass = definingClass
                val helperName = "patch_handleAutoPlay"
                val helperReturnType = "Ljava/lang/Enum;"
                val helperMethod = ImmutableMethod(
                    helperClass,
                    helperName,
                    listOf(ImmutableMethodParameter("Ljava/lang/Enum;", null, null)),
                    helperReturnType,
                    AccessFlags.PRIVATE.value,
                    null,
                    null,
                    MutableMethodImplementation(7),
                ).toMutable().apply {
                    addInstructionsWithLabels(
                        0,
                        """
                            invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->isAutoPlay(Ljava/lang/Enum;)Z
                            move-result v0
                            if-eqz v0, :ignore
                            new-instance v0, ${userActionMethodReference.definingClass}
                            const/4 v1, 0x3
                            const/4 v2, 0x0
                            invoke-direct { v0, v1, v2, v2 }, $userActionMethodReference
                            iget-object v3, p0, $getReelSequenceControllerReference
                            invoke-virtual { v3, v0 }, $reelSequenceControllerMethodReference
                            const/4 v4, 0x0
                            return-object v4
                            :ignore
                            return-object p1
                        """
                    )
                }
                reelPlaybackRepeatFingerprint.classDef.methods.add(helperMethod)

                addInstructionsWithLabels(
                    extensionReturnResultIndex + 1,
                    """
                        invoke-direct { p0, v$enumRegister }, $helperClass->$helperName(Ljava/lang/Enum;)$helperReturnType
                        move-result-object v$enumRegister
                        if-nez v$enumRegister, :ignore
                        return-void     # Autoplay was performed.
                        :ignore
                        nop
                    """
                )
            }
        }
    }
}
