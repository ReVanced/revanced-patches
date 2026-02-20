package app.revanced.patches.gamehub.misc.tokenexpiry

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val bypassTokenExpiryPatch = bytecodePatch {
    execute {
        routerUtilsTokenExpiryFingerprint.method.returnEarly()
        routerUtilsGuideLoginFingerprint.method.returnEarly()

        // In checkGuideStep$1.invokeSuspend, after the XjLog.h() debug log call,
        // inject the same goto as the reference diff: skip the entire guide-step
        // validation block and jump directly to the DeviceManager readiness check.
        // The two const pre-loads set up registers that the DeviceManager block
        // expects to find initialised (v4 = null BooleanRef guard, v3 = FLAG_ACTIVITY_NEW_TASK).
        routerUtilsGuideStepFingerprint.method.apply {
            val logCallIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_STATIC &&
                    getReference<MethodReference>()?.let {
                        it.definingClass == "Lcom/xj/common/utils/XjLog;" && it.name == "h"
                    } == true
            }

            val deviceManagerInstruction = instructions[
                indexOfFirstInstructionOrThrow(logCallIndex) {
                    opcode == Opcode.SGET_OBJECT &&
                        getReference<FieldReference>()?.let {
                            it.definingClass ==
                                "Lcom/xj/bussiness/devicemanagement/utils/DeviceManager;" &&
                                it.name == "a"
                        } == true
                }
            ]

            addInstructionsWithLabels(
                logCallIndex + 1,
                """
                    const/4 v4, 0x0
                    const/high16 v3, 0x10000000
                    goto/16 :skip_guide_check
                """,
                ExternalLabel("skip_guide_check", deviceManagerInstruction),
            )
        }
    }
}
