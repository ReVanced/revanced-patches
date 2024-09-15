package app.revanced.patches.youtube.layout.startupshortsreset.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsConfigFingerprint.indexOfOptionalInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * 18.15.40+
 */
internal object UserWasInShortsConfigFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Failed to get offline response: "),
    customFingerprint = { methodDef, _ ->
        indexOfOptionalInstruction(methodDef) >= 0
    }
) {
    fun indexOfOptionalInstruction(methodDef: Method) =
        methodDef.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            opcode == Opcode.INVOKE_STATIC &&
                    reference?.definingClass == "Lj${'$'}/util/Optional;" &&
                    reference.name == "of" &&
                    reference.returnType == "Lj${'$'}/util/Optional;"
        }
}