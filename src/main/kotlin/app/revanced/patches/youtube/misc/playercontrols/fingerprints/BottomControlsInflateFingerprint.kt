package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object BottomControlsInflateFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.SYNTHETIC,
    returnType = "L",
    parameters = listOf(),
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { BottomControlsResourcePatch.bottomUiContainerResourceId }
)