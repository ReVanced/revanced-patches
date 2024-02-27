package app.revanced.patches.music.layout.overlayfilter.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.DesignBottomSheetDialog
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object DesignBottomSheetDialogFingerprint : LiteralValueFingerprint(
    returnType = "V",
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { DesignBottomSheetDialog }
)

