package app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints

import app.revanced.patches.youtube.layout.hide.floatingmicrophone.fabButtonId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val showFloatingMicrophoneButtonFingerprint = literalValueFingerprint(
    literalSupplier = { fabButtonId },
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
