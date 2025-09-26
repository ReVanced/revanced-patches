package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

context(MutableMethod, BytecodePatchContext)
internal fun editShareLinksPatch(block: (index: Int, register: Int) -> Unit) {
    val putSharingUrlIndex = indexOfFirstInstruction(
        permalinkResponseJsonParserFingerprint.stringMatches!!.first { it.string == "permalink" }.index,
        Opcode.IPUT_OBJECT
    )

    val sharingUrlRegister = getInstruction<TwoRegisterInstruction>(putSharingUrlIndex).registerA

    block(putSharingUrlIndex, sharingUrlRegister)
}
