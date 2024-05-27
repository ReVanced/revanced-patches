package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patches.youtube.misc.playercontrols.bottomUiContainerResourceId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val bottomControlsInflateFingerprint = literalValueFingerprint(
    literalSupplier = { bottomUiContainerResourceId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.SYNTHETIC)
    returns("L")
    parameters()
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
}
