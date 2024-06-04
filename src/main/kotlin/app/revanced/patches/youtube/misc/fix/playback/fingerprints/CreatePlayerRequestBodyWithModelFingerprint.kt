package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal fun indexOfBuildModelInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<FieldReference>()
        reference?.definingClass == "Landroid/os/Build;" &&
            reference.name == "MODEL" &&
            reference.type == "Ljava/lang/String;"
    }

internal val createPlayerRequestBodyWithModelFingerprint = methodFingerprint {
    returns("L")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    custom { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(1073741824) && indexOfBuildModelInstruction(methodDef) >= 0
    }
}
