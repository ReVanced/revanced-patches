package app.revanced.patches.youtube.misc.video.quality.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object VideoQualityReferenceFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L"), listOf(
        Opcode.IPUT_OBJECT, Opcode.RETURN_VOID
    )
)