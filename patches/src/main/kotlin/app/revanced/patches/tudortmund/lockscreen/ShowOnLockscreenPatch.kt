package app.revanced.patches.tudortmund.lockscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tudortmund.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tudortmund/lockscreen/ShowOnLockscreenPatch;"

@Suppress("unused")
val showOnLockscreenPatch = bytecodePatch(
    name = "Show on lockscreen",
    description = "Shows student id and student ticket on lockscreen.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith("de.tudortmund.app")

    val brightnessMatch by brightnessFingerprint()

    execute {
        brightnessMatch.mutableMethod.apply {
            // Find the instruction where the brightness value is loaded into a register
            val brightnessInstruction = instructions.firstNotNullOf { instruction ->
                if (instruction.opcode != Opcode.IGET_OBJECT) return@firstNotNullOf null

                val getInstruction = instruction as Instruction22c
                val fieldReference = getInstruction.reference as FieldReference

                if (fieldReference.type != "Ljava/lang/Float;") return@firstNotNullOf null

                instruction
            }

            // Search for the instruction where we get the android.view.Window via the Activity.
            // Gets the index of that instruction and the register of the Activity.
            val (windowIndex, activityRegister) = implementation!!.instructions.withIndex()
                .firstNotNullOf { (index, instruction) ->
                    if (instruction.opcode != Opcode.INVOKE_VIRTUAL) {
                        return@firstNotNullOf null
                    }

                    val invokeInstruction = instruction as Instruction35c
                    val methodRef = invokeInstruction.reference as MethodReference

                    if (methodRef.name != "getWindow" || methodRef.returnType != "Landroid/view/Window;") {
                        return@firstNotNullOf null
                    }

                    Pair(index, invokeInstruction.registerC)
                }

            // The register in which the brightness value is loaded
            val brightnessRegister = brightnessInstruction.registerA
            // Replaces the getWindow call with our custom one to run the lockscreen code
            replaceInstruction(
                windowIndex,
                "invoke-static { v$activityRegister, v$brightnessRegister }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "getWindow" +
                    "(Landroidx/appcompat/app/AppCompatActivity;F)" +
                    "Landroid/view/Window;",
            )

            // Normally, the brightness is loaded into a register after the getWindow call.
            // In order to pass the brightness value to our custom getWindow implementation,
            // we need to add the same instructions before the getWindow call.
            // The Float object is loaded into the brightness register and gets converted to a float.
            addInstructions(
                windowIndex,
                """
                    invoke-virtual { v$brightnessRegister }, Ljava/lang/Float;->floatValue()F
                    move-result v$brightnessRegister
                """,
            )

            addInstruction(windowIndex, brightnessInstruction)
        }
    }
}
