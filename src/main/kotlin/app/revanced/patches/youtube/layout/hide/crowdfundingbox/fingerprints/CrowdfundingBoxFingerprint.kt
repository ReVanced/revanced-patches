package app.revanced.patches.youtube.layout.hide.crowdfundingbox.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.CrowdfundingBoxResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object CrowdfundingBoxFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    ),
    literalSupplier = { CrowdfundingBoxResourcePatch.crowdfundingBoxId }
)