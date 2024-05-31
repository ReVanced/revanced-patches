package app.revanced.patches.youtube.video.quality.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves with the class found in [videoQualitySetterFingerprint].
 */
internal val setQualityByIndexMethodClassFieldReferenceFingerprint = methodFingerprint {
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
    )
}
