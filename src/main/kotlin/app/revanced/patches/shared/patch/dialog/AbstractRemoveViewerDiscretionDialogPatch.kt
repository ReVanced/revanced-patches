package app.revanced.patches.shared.patch.dialog

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.fingerprints.dialog.CreateDialogFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

abstract class AbstractRemoveViewerDiscretionDialogPatch(
    private val classDescriptor: String,
    private val additionalFingerprints: Set<MethodFingerprint> = emptySet()
) : BytecodePatch(
    buildSet {
        add(CreateDialogFingerprint)
        additionalFingerprints.let(::addAll)
    }
) {
    private fun MutableMethod.invoke(isAgeVerified: Boolean) {
        val showDialogIndex = implementation!!.instructions.indexOfFirst { instruction ->
            ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.name == "show"
        }
        val dialogRegister = getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

        val methodName =
            if (isAgeVerified)
                "confirmDialogAgeVerified"
            else
                "confirmDialog"

        addInstruction(
            showDialogIndex + 1,
            "invoke-static { v$dialogRegister }, $classDescriptor->$methodName(Landroid/app/AlertDialog;)V"
        )
    }

    override fun execute(context: BytecodeContext) {
        CreateDialogFingerprint.result?.mutableMethod?.invoke(false)
            ?: throw CreateDialogFingerprint.exception

        if (additionalFingerprints.isNotEmpty()) {
            additionalFingerprints.forEach { fingerprint ->
                fingerprint.result?.let {
                    val targetMethod = context.toMethodWalker(it.method)
                        .nextMethod(it.scanResult.patternScanResult!!.endIndex - 1, true)
                        .getMethod() as MutableMethod

                    targetMethod.invoke(true)
                } ?: throw fingerprint.exception
            }
        }

    }
}