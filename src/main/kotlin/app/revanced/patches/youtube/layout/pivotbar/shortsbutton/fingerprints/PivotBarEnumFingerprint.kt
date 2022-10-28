package app.revanced.patches.youtube.layout.pivotbar.shortsbutton.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("pivot-bar-enum-fingerprint")
@ShortsButtonCompatibility
@Version("0.0.1")
object PivotBarEnumFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IGET,
        Opcode.AND_INT_LIT8, // unique instruction anchor
    )
)