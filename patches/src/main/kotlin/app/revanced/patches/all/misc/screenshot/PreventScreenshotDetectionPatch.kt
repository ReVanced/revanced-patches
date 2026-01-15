package app.revanced.patches.all.misc.screenshot

import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

/** `Landroid/app/Activity;->registerScreenCaptureCallback(Ljava/util/concurrent/Executor;Landroid/app/Activity$ScreenCaptureCallback;)V` */
private val registerScreenCaptureCallbackMethodReference = ImmutableMethodReference(
    "Landroid/app/Activity;",
    "registerScreenCaptureCallback",
    listOf(
        "Ljava/util/concurrent/Executor;",
        "Landroid/app/Activity\$ScreenCaptureCallback;",
    ),
    "V"
)

/** `Landroid/app/Activity;->unregisterScreenCaptureCallback(Landroid/app/Activity$ScreenCaptureCallback;)V` */
private val unregisterScreenCaptureCallbackMethodReference = ImmutableMethodReference(
    "Landroid/app/Activity;",
    "unregisterScreenCaptureCallback",
    listOf(
        "Landroid/app/Activity\$ScreenCaptureCallback;",
    ),
    "V"
)

@Suppress("unused")
val preventScreenshotDetectionPatch = bytecodePatch(
    name = "Prevent screenshot detection",
    description = "Removes the registration of all screen capture callbacks. This prevents the app from detecting screenshots.",
) {
    dependsOn(transformInstructionsPatch(
        filterMap = { _, _, instruction, instructionIndex ->
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) {
                return@transformInstructionsPatch null
            }
            val reference = instruction.getReference<MethodReference>() ?: return@transformInstructionsPatch null
            if (!MethodUtil.methodSignaturesMatch(reference, registerScreenCaptureCallbackMethodReference) &&
                !MethodUtil.methodSignaturesMatch(reference, unregisterScreenCaptureCallbackMethodReference )
            ) {
                return@transformInstructionsPatch null
            }

            instructionIndex
        },
        transform = { mutableMethod, instructionIndex ->
            mutableMethod.removeInstruction(instructionIndex)
        }
    ))
}
