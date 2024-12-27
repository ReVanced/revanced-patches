package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val formFactorEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "UNKNOWN_FORM_FACTOR",
        "SMALL_FORM_FACTOR",
        "LARGE_FORM_FACTOR",
        "AUTOMOTIVE_FORM_FACTOR"
    )
}

internal val createPlayerRequestBodyWithModelFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    opcodes(Opcode.OR_INT_LIT16)
    custom { method, _ ->
        method.indexOfModelInstruction() >= 0 &&
                method.indexOfReleaseInstruction() >= 0
    }
}

private fun Method.indexOfModelInstruction() =
    indexOfFirstInstruction {
        val reference = getReference<FieldReference>()

        reference?.definingClass == "Landroid/os/Build;" &&
                reference.name == "MODEL" &&
                reference.type == "Ljava/lang/String;"
    }

internal fun Method.indexOfReleaseInstruction(): Int =
    indexOfFirstInstruction {
        val reference = getReference<FieldReference>()

        reference?.definingClass == "Landroid/os/Build${'$'}VERSION;" &&
                reference.name == "RELEASE" &&
                reference.type == "Ljava/lang/String;"
    }

