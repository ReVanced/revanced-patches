package app.revanced.patches.all.misc.screenshot

import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val preventScreenshotDetectionPatch = bytecodePatch(
    name = "Prevent screenshot detection",
    description = "Prevents the app from detecting screenshots.",
) {
    dependsOn(transformInstructionsPatch(
        filterMap = { _, _, instruction, instructionIndex ->
            // invoke-virtual Landroid/app/Activity;->registerScreenCaptureCallback(Ljava/util/concurrent/Executor;Landroid/app/Activity$ScreenCaptureCallback;)V
            // invoke-virtual Landroid/app/Activity;->unregisterScreenCaptureCallback(Landroid/app/Activity$ScreenCaptureCallback;)V
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) {
                null
            } else {
                val reference = instruction.getReference<MethodReference>()
                if (reference != null &&
                    reference.returnType == "V" &&
                    reference.definingClass == "Landroid/app/Activity;" && (
                        reference.name == "registerScreenCaptureCallback" &&
                        reference.parameterTypes == listOf(
                            "Ljava/util/concurrent/Executor;",
                            "Landroid/app/Activity\$ScreenCaptureCallback;",
                        ) || (
                        reference.name == "unregisterScreenCaptureCallback" &&
                        reference.parameterTypes == listOf(
                            "Landroid/app/Activity\$ScreenCaptureCallback;",
                        )
                    ))
                ) {
                    instructionIndex
                } else {
                    null
                }
            }
        },
        transform = { mutableMethod, instructionIndex ->
            mutableMethod.removeInstruction(instructionIndex)
        }
    ))
}
