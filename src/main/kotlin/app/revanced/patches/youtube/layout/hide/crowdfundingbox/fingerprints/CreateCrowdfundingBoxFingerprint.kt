package app.revanced.patches.youtube.layout.hide.crowdfundingbox.fingerprints

import app.revanced.patches.youtube.layout.hide.crowdfundingbox.crowdfundingBoxId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

val createCrowdfundingBoxFingerprint = literalValueFingerprint(
    literalSupplier = { crowdfundingBoxId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    )
}
