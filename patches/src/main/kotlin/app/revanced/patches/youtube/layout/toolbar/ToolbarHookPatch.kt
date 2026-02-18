package app.revanced.patches.youtube.layout.toolbar

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.getToolBarButtonMethodMatch
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ToolbarPatch;"

internal lateinit var hookToolbar: (descriptor: String)-> Unit

val toolbarHookPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        resourceMappingPatch
    )

    apply {
        fun indexOfGetDrawableInstruction(method: Method) =
            method.indexOfFirstInstruction {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.toString() == "Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;"
            }

        getToolBarButtonMethodMatch().method.apply {
            val getDrawableIndex = indexOfGetDrawableInstruction(this)
            val enumOrdinalIndex = indexOfFirstInstructionReversedOrThrow(getDrawableIndex) {
                opcode == Opcode.INVOKE_INTERFACE &&
                        getReference<MethodReference>()?.returnType == "I"
            }
            val replaceReference = getInstruction<ReferenceInstruction>(enumOrdinalIndex).reference
            val replaceRegister = getInstruction<FiveRegisterInstruction>(enumOrdinalIndex).registerC
            val enumRegister = getInstruction<FiveRegisterInstruction>(enumOrdinalIndex).registerD
            val insertIndex = enumOrdinalIndex + 1
            val freeRegister = findFreeRegister(insertIndex, enumRegister, replaceRegister)

            val imageViewIndex = indexOfFirstInstructionOrThrow(enumOrdinalIndex) {
                opcode == Opcode.IGET_OBJECT && fieldReference?.type == "Landroid/widget/ImageView;"
            }
            val imageViewReference = getInstruction<ReferenceInstruction>(imageViewIndex).reference

            addInstructions(
                insertIndex,
                """
                    iget-object v$freeRegister, p0, $imageViewReference
                    invoke-static {v$enumRegister, v$freeRegister}, $EXTENSION_CLASS_DESCRIPTOR->hookToolbar(Ljava/lang/Enum;Landroid/widget/ImageView;)V
                    invoke-interface {v$replaceRegister, v$enumRegister}, $replaceReference
                """
            )
            removeInstruction(enumOrdinalIndex)
        }

        hookToolbar = { descriptor ->
            hookToolbarMethod.addInstructions(
                0,
                "invoke-static {p0, p1}, $descriptor(Ljava/lang/String;Landroid/view/View;)V"
            )
        }
    }
}