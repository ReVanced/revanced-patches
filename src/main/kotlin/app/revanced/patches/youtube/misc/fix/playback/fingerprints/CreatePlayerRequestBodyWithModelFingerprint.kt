package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.CreatePlayerRequestBodyWithModelFingerprint.indexOfBuildModelInstruction
import app.revanced.util.containsWideLiteralInstructionValue
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal object CreatePlayerRequestBodyWithModelFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(1073741824) &&
                indexOfBuildModelInstruction(methodDef) >= 0
    },
) {
    fun indexOfBuildModelInstruction(methodDef: Method) =
        methodDef.indexOfFirstInstruction {
            val reference = getReference<FieldReference>()
            reference?.definingClass == "Landroid/os/Build;" &&
                    reference.name == "MODEL" &&
                    reference.type == "Ljava/lang/String;"
        }
}


