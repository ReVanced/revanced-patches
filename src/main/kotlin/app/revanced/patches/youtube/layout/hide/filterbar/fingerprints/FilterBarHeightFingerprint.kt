package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patches.youtube.layout.hide.filterbar.filterBarHeightId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val filterBarHeightFingerprint = literalValueFingerprint(
    literalSupplier = { filterBarHeightId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT,
    )
}
