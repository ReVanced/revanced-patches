package app.revanced.patches.soundcloud.shared

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val featureConstructorFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Ljava/lang/String;", "Z", "Ljava/util/List;")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL
    )
}
