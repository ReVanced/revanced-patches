package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patches.youtube.layout.returnyoutubedislike.oldUIDislikeId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val dislikesOldLayoutTextViewFingerprint = literalValueFingerprint(
    literalSupplier = { oldUIDislikeId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.CONST, // resource identifier register
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ, // textview register
        Opcode.GOTO,
    )
}
