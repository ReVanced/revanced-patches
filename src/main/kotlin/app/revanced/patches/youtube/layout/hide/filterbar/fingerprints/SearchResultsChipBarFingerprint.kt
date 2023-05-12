package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patches.youtube.layout.hide.filterbar.patch.HideFilterBarResourcePatch.Companion.barContainerHeightId
import org.jf.dexlib2.Opcode

object SearchResultsChipBarFingerprint : LiteralOpcodesFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    barContainerHeightId
)