package app.revanced.patches.youtube.video.hdr.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

object HdrCapabilitiesFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf("I", "Landroid/view/Display;"),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.implementation == null)
            return@custom false

        for (instruction in methodDef.implementation!!.instructions) {
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL)
                continue

            val objectInstruction = instruction as ReferenceInstruction
            if ((objectInstruction.reference as MethodReference).name != "getSupportedHdrTypes")
                continue

            return@custom true
        }
        return@custom false
    }
)
