package app.revanced.patches.youtube.misc.ambientmode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

object PowerSaveModeFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Ljava/lang/Object;"),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.name != "accept")
            return@custom false

        val instructions = methodDef.implementation?.instructions!!

        if (instructions.count() < 20)
            return@custom false

        for (instruction in instructions) {
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL)
                continue

            val invokeInstruction = instruction as Instruction35c
            if ((invokeInstruction.reference as MethodReference).name == "isPowerSaveMode")
                return@custom true
        }
        return@custom false
    }
)