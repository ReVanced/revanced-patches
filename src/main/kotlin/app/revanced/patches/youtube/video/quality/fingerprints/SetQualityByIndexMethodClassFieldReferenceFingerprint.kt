package app.revanced.patches.youtube.video.quality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves with the class found in [VideoQualitySetterFingerprint].
 */
internal object SetQualityByIndexMethodClassFieldReferenceFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
    )
)