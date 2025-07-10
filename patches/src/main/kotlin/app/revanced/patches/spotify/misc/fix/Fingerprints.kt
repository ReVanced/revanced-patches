package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val loadOrbitLibraryFingerprint = fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val extensionFixConstantsFingerprint = fingerprint {
    custom { _, classDef -> classDef.type == "Lapp/revanced/extension/spotify/misc/fix/Constants;" }
}

internal val runIntegrityVerificationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_STATIC, // Calendar.getInstance()
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL, // instance.get(6)
        Opcode.MOVE_RESULT,
        Opcode.IF_EQ, // if (x == instance.get(6)) return
    )
    custom { method, _ ->
        method.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.definingClass == "Ljava/util/Calendar;" && reference.name == "get"
        } >= 0
    }
}
