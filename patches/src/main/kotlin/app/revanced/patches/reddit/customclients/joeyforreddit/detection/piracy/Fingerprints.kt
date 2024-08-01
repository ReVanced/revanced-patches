package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val piracyDetectionFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.CONST_16,
        Opcode.CONST_WIDE_16,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
    custom { _, classDef ->
        classDef.endsWith("ProcessLifeCyleListener;")
    }
}