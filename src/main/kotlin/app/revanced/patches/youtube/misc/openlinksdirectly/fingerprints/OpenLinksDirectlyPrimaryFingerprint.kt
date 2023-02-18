package app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.openlinksdirectly.annotations.OpenLinksDirectlyCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@OpenLinksDirectlyCompatibility
object OpenLinksDirectlyPrimaryFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L"), listOf(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.SGET
    )
)
