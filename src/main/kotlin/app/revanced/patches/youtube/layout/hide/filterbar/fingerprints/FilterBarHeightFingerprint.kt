package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patches.youtube.layout.hide.filterbar.patch.HideFilterBarResourcePatch.Companion.filterBarHeightId
import org.jf.dexlib2.Opcode

object FilterBarHeightFingerprint : LiteralOpcodesFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT
    ),
    filterBarHeightId
)