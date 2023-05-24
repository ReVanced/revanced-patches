package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patches.youtube.layout.hide.filterbar.patch.HideFilterBarResourcePatch.Companion.relatedChipCloudMarginId
import org.jf.dexlib2.Opcode

object RelatedChipCloudFingerprint : LiteralOpcodesFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    relatedChipCloudMarginId
)