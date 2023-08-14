package app.revanced.patches.youtube.video.videoid.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object VideoIdFingerprintBackgroundPlay : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.DECLARED_SYNCHRONIZED or AccessFlags.FINAL or AccessFlags.PUBLIC,
    parameters = listOf("L"),
    opcodes = listOf(Opcode.INVOKE_INTERFACE),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("PlaybackLifecycleMonitor;")
    }
)
