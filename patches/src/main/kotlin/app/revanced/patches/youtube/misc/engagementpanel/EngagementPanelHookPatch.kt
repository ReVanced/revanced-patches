package app.revanced.patches.youtube.misc.engagementpanel

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/EngagementPanelHookPatch;"

internal lateinit var engagementPanelBuilderMethod: Method
internal var engagementPanelFreeRegister = 0
internal var engagementPanelIdIndex = 0
internal var engagementPanelIdRegister = 0

val engagementPanelHookPatch = bytecodePatch(
    description = "Hook to get the current engagement panel type like video description."
) {
    dependsOn(
        sharedExtensionPatch,
        resourceMappingPatch,
    )

    execute {
        fun Method.setFreeIndex(startIndex: Int) {
            val startRegister = engagementPanelIdRegister
            var index = startIndex
            var register = startRegister

            while (register == startRegister) {
                index = indexOfFirstInstructionOrThrow(index + 1, Opcode.IGET_OBJECT)
                register = getInstruction<TwoRegisterInstruction>(index).registerA
            }

            engagementPanelFreeRegister = register
        }

        val (engagementPanelIdReference, engagementPanelObjectReference) =
            with(engagementPanelLayoutFingerprint.method) {
                val engagementPanelIdIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type == "Ljava/lang/String;"
                }
                val engagementPanelObjectIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type != "Ljava/lang/String;"
                }
                Pair(
                    getInstruction<ReferenceInstruction>(engagementPanelIdIndex).reference.toString(),
                    getInstruction<ReferenceInstruction>(engagementPanelObjectIndex).reference.toString(),
                )
            }

        engagementPanelBuilderFingerprint.method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IGET_OBJECT &&
                        getReference<FieldReference>()?.toString() == engagementPanelObjectReference
            }
            val insertInstruction = getInstruction<TwoRegisterInstruction>(insertIndex)
            val classRegister = insertInstruction.registerB
            engagementPanelIdRegister = insertInstruction.registerA

            setFreeIndex(insertIndex)

            addInstructions(
                insertIndex, """
                    iget-object v$engagementPanelIdRegister, v$classRegister, $engagementPanelIdReference
                    invoke-static {v$engagementPanelIdRegister}, $EXTENSION_CLASS_DESCRIPTOR->setId(Ljava/lang/String;)V
                    """
            )
            engagementPanelIdIndex = insertIndex + 1
            engagementPanelBuilderMethod = this
        }

        engagementPanelUpdateFingerprint
            .method
            .addInstruction(
                0,
                "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hide()V"
            )
    }
}
