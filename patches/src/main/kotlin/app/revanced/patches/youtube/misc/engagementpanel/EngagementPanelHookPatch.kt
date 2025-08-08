package app.revanced.patches.youtube.misc.engagementpanel

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/EngagementPanelHookPatch;"

private var engagementPanelIdReference: String? = null
private var engagementPanelObjectReference: String? = null
private var engagementPanelIdRegister = 0
private var engagementPanelFreeRegister = 0

private val engagementPanelResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        // Map engagement panel resource IDs if needed
    }
}

val engagementPanelHookPatch = bytecodePatch(
    description = "Hook to capture Engagement Panel ID and hide it when needed."
) {
    dependsOn(
        sharedExtensionPatch,
        engagementPanelResourcePatch
    )

    execute {
        fun setFreeIndex(startIndex: Int) {
            var index = startIndex
            var register = engagementPanelIdRegister

            while (register == engagementPanelIdRegister) {
                index = engagementPanelBuilderFingerprint.method.indexOfFirstInstruction(index + 1, Opcode.IGET_OBJECT)
                register = engagementPanelBuilderFingerprint.method
                    .getInstruction<TwoRegisterInstruction>(index).registerA
            }
            engagementPanelFreeRegister = register
        }

        engagementPanelLayoutFingerprint.method.apply {
            val idIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT &&
                        (this as ReferenceInstruction).reference.let { ref ->
                            ref is com.android.tools.smali.dexlib2.iface.reference.FieldReference &&
                                    ref.type == "Ljava/lang/String;"
                        }
            }
            val objectIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT &&
                        (this as ReferenceInstruction).reference.let { ref ->
                            ref is com.android.tools.smali.dexlib2.iface.reference.FieldReference &&
                                    ref.type != "Ljava/lang/String;"
                        }
            }

            engagementPanelIdReference = (getInstruction<ReferenceInstruction>(idIndex).reference).toString()
            engagementPanelObjectReference = (getInstruction<ReferenceInstruction>(objectIndex).reference).toString()
        }

        engagementPanelBuilderFingerprint.method.apply {
            val insertIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IGET_OBJECT &&
                        (this as ReferenceInstruction).reference.toString() == engagementPanelObjectReference
            }
            val insertInstruction = getInstruction<TwoRegisterInstruction>(insertIndex)
            val classRegister = insertInstruction.registerB
            engagementPanelIdRegister = insertInstruction.registerA

            setFreeIndex(insertIndex)

            addInstructions(
                insertIndex,
                """
                    iget-object v$engagementPanelIdRegister, v$classRegister, $engagementPanelIdReference
                    invoke-static {v$engagementPanelIdRegister}, $EXTENSION_CLASS_DESCRIPTOR->setId(Ljava/lang/String;)V
                """
            )
        }

        engagementPanelUpdateFingerprint.method.addInstruction(
            0,
            "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hide()V"
        )
    }
}
