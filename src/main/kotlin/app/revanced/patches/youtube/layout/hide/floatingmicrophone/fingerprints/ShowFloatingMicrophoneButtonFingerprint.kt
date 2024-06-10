package app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.fabButtonId
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val showFloatingMicrophoneButtonFingerprint = methodFingerprint(
    literal { fabButtonId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.RETURN_VOID,
    )
}
