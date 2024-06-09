package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val piracyDetectionFingerprint = methodFingerprint {
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