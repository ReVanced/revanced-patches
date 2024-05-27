package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.reelMultipleItemShelfId
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val reelConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.INVOKE_VIRTUAL)
    custom { methodDef, _ ->
        // Cannot use LiteralValueFingerprint, because the resource id may not be present.
        val reelMultipleItemShelfId = reelMultipleItemShelfId
        reelMultipleItemShelfId != -1L &&
            methodDef.containsWideLiteralInstructionValue(reelMultipleItemShelfId)
    }
}
