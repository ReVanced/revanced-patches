package app.revanced.patches.youtube.misc.privacy.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val youtubeShareSheetFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_VIRTUAL,
    )
}
