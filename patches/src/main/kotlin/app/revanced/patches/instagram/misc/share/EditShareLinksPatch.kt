package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstStringInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.mutable.MutableMethod

context(_: BytecodePatchContext)
internal fun editShareLinksPatch(block: MutableMethod.(index: Int, register: Int) -> Unit) {
    val methodsToPatch = arrayOf(
        permalinkResponseJsonParserMethod,
        storyUrlResponseJsonParserMethod,
        profileUrlResponseJsonParserMethod,
        liveUrlResponseJsonParserMethod,
    )

    for (method in methodsToPatch) {
        method.apply {
            val putSharingUrlIndex = indexOfFirstInstruction(
                indexOfFirstStringInstruction("permalink"),
                Opcode.IPUT_OBJECT,
            )

            val sharingUrlRegister = getInstruction<TwoRegisterInstruction>(putSharingUrlIndex).registerA

            block(putSharingUrlIndex, sharingUrlRegister)
        }
    }
}
