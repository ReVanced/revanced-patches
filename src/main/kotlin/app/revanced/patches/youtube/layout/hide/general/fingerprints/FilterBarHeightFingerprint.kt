package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object FilterBarHeightFingerprint : LiteralValueFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT
    ),
    literalSupplier = { HideLayoutComponentsResourcePatch.filterBarHeightId }
)