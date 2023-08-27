package app.revanced.patches.youtube.video.quality.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves with the class found in [VideoQualitySetterFingerprint].
 */
object SetQualityByIndexMethodClassFieldReferenceFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
)