package app.revanced.patches.music.audio.codecs.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// FIXME: Test this threshold and find the best value.
internal val codecsLockFingerprint = methodFingerprint(fuzzyPatternScanThreshold = 2) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_OBJECT
    )
    strings("eac3_supported")
}