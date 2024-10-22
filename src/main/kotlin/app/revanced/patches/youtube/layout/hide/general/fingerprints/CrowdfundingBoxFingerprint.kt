package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CrowdfundingBoxFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    ),
    literalSupplier = { HideLayoutComponentsResourcePatch.crowdfundingBoxId }
)