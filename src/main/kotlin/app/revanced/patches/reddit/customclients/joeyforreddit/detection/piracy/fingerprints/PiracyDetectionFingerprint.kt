package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object PiracyDetectionFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    opcodes = listOf(
        Opcode.NEW_INSTANCE,    // new PiracyDetectionRunnable()
        Opcode.CONST_16,
        Opcode.CONST_WIDE_16,
        Opcode.INVOKE_DIRECT,   // <init>(..)
        Opcode.INVOKE_VIRTUAL,  // run()
        Opcode.RETURN_VOID
    ),
    customFingerprint = custom@{ _, classDef ->
        classDef.type.endsWith("ProcessLifeCyleListener;")
    }
)