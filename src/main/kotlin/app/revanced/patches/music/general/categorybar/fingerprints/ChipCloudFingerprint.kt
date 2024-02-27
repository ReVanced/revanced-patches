package app.revanced.patches.music.general.categorybar.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.ChipCloud
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object ChipCloudFingerprint : LiteralValueFingerprint(
    returnType = "V",
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { ChipCloud }
)

