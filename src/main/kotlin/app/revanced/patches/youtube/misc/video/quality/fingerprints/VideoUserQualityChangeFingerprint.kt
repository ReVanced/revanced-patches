
package app.revanced.patches.youtube.misc.video.quality.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object VideoUserQualityChangeFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("L","L","I","J"),
    listOf(
        Opcode.MOVE,
        Opcode.MOVE_WIDE,
        Opcode.INVOKE_INTERFACE_RANGE,
        Opcode.RETURN_VOID
    )
)