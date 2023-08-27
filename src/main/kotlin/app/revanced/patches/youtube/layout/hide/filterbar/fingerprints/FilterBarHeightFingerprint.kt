package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.filterbar.patch.HideFilterBarResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object FilterBarHeightFingerprint : LiteralValueFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT
    ),
    literal = HideFilterBarResourcePatch.filterBarHeightId
)