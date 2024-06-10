package app.revanced.patches.youtube.misc.backgroundplayback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.misc.backgroundplayback.prefBackgroundAndOfflineCategoryId
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val backgroundPlaybackSettingsFingerprint = methodFingerprint(
    literal { prefBackgroundAndOfflineCategoryId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IF_NEZ,
        Opcode.GOTO,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
    )
}
