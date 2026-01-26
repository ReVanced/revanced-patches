package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.mutable.MutableMethod

internal fun BytecodePatchContext.editShareLinksPatch(block: MutableMethod.(index: Int, register: Int) -> Unit) {
    val methodsToPatch = arrayOf(
        permalinkResponseJsonParserMethodMatch,
        storyUrlResponseJsonParserMethodMatch,
        profileUrlResponseJsonParserMethodMatch,
        liveUrlResponseJsonParserMethodMatch,
    )

    for (match in methodsToPatch) {
        match.method.apply {
            val putSharingUrlIndex = indexOfFirstInstruction(
                match.indices.first(),
                Opcode.IPUT_OBJECT,
            )

            val sharingUrlRegister = getInstruction<TwoRegisterInstruction>(putSharingUrlIndex).registerA

            block(putSharingUrlIndex, sharingUrlRegister)
        }
    }
}
