package app.revanced.patches.youtube.misc.openlinksdirectly.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.openlinksdirectly.annotations.OpenLinksDirectlyCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("open-links-directly-secondary-fingerprint")
@OpenLinksDirectlyCompatibility
@Version("0.0.1")
object OpenLinksDirectlyFingerprintSecondary : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L"), listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.RETURN_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.CONST_STRING
    ),
    strings = listOf("Uri must have an absolute scheme")
)
