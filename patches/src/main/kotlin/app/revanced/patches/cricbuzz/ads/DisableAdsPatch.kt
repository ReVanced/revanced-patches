package app.revanced.patches.cricbuzz.ads

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.cricbuzz.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/cricbuzz/ads/HideAdsPatch;"

@Suppress("unused")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.cricbuzz.android"("6.24.01"))

    dependsOn(sharedExtensionPatch)

    apply {
        userStateSwitchMethod.returnEarly(true)

        // Remove region-specific Cricbuzz11 elements.
        cb11ConstructorMethod.addInstruction(0, "const/4 p7, 0x0")
        getBottomBarMethod.apply {
            val getIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.name == "bottomBar"
            }
            val getRegister = getInstruction<TwoRegisterInstruction>(getIndex).registerA

            addInstruction(
                getIndex + 1,
                "invoke-static { v$getRegister }, $EXTENSION_CLASS_DESCRIPTOR->filterCb11(Ljava/util/List;)V"
            )
        }
    }
}
