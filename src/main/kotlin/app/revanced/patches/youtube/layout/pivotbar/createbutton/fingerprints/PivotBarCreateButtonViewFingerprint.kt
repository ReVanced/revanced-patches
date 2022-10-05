package app.revanced.patches.youtube.layout.pivotbar.createbutton.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.pivotbar.createbutton.annotations.CreateButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("pivot-bar-create-button-view-fingerprint")
@CreateButtonCompatibility
@Version("0.0.1")
object PivotBarCreateButtonViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_DIRECT_RANGE, // unique instruction anchor
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    )
)